package app.extractor;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.InternalException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

import app.config.LoggingConfig;
import app.logging.IStackSerializer;
import app.logging.StackSerializerJson;

import com.sun.jdi.ReferenceType;

/**
 * This class extract all the information of a given stack frame to a text file
 */
public class StackFrameLogger {

	public static Map<String, Class<? extends IStackSerializer>> serializerChoice = registerAllSerializer();

	/**
	 * Save all serialized frame before logging
	 */
	private Stack<String> serializedFrames;

	/**
	 * The logger used to collect extracted information
	 */
	private IStackSerializer serializer;

	/**
	 * The logging configuration used
	 */
	private LoggingConfig loggingConfig;

	/**
	 * represent the maximum recursion algorithm to study object's fields and
	 * array's value can make
	 */
	private int maxDepth;

	/**
	 * Used to indicates which Object has already been visited, to not visit again.
	 */
	private Set<ObjectReference> visited = new HashSet<>();

	/**
	 * whether the frames are independents with each other, if they are, visited is
	 * reseted between each frame serialisation
	 */
	private boolean frameIndependents;

	/**
	 * Constructor of StackFrameSerializer
	 * 
	 * @param loggingConfig information to instantiate the logger
	 * @param depth         the max depth of the object graphs
	 * @param
	 */
	public StackFrameLogger(LoggingConfig loggingConfig, int depth, boolean frameIndependents) {
		this.serializedFrames = new Stack<>();
		this.loggingConfig = loggingConfig;
		this.maxDepth = depth;
		this.frameIndependents = frameIndependents;

		try {
			// Finding the class corresponding to the format configuration
			Class<? extends IStackSerializer> serializerClass = serializerChoice.get(loggingConfig.getFormat());

			// Finding the constructor
			Constructor<? extends IStackSerializer> constructor = serializerClass.getConstructor();

			// Creation of the instance
			this.serializer = constructor.newInstance();

		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to instantiate the serializer for the format : " + loggingConfig.getFormat(), e);
		}
	}

	public static Map<String, Class<? extends IStackSerializer>> registerAllSerializer() {
		Map<String, Class<? extends IStackSerializer>> res = new HashMap<>();
		// json format
		res.put("json", StackSerializerJson.class);
		return res;
	}

	/**
	 * Returns the used logger
	 * 
	 * @return the used logger
	 */
	public IStackSerializer getLogger() {
		return serializer;
	}

	/**
	 * extract a frame, by extracting the method signature, its arguments, and its
	 * receiver
	 * 
	 * @param frame the frame to extract
	 */
	public String extract(StackFrame frame) {
		if (this.frameIndependents) {
			this.visited = new HashSet<>();
		}
		String res = "";
		res += this.serializer.frameLineStart(serializedFrames.size());
		res += extractMethod(frame);
		res += serializer.joinElementListing();
		res += extractArguments(frame);
		res += serializer.joinElementListing();
		res += extractReceiver(frame);
		res += this.serializer.frameLineEnd();

		return res;
	}

	/**
	 * Extracting the method signature used in the given frame
	 * 
	 * @param frame the frame to extract
	 */
	public String extractMethod(StackFrame frame) {
		Method method = frame.location().method();
		return serializer.methodSignature(method);
	}

	/**
	 * Extracting all accessible arguments given in the method in this frame
	 * 
	 * @param frame the frame to extract
	 */
	public String extractArguments(StackFrame frame) {
		boolean exception = false;
		String res = "";
		res += serializer.methodArgumentsStart();

		// arguments can sometimes not be accessible, if that's the case, stop here
		Iterator<Value> argumentsValueIterator = null;
		try {
			argumentsValueIterator = frame.getArgumentValues().iterator();
		} catch (InternalException e) {
			// Happens for native calls, and can't be obtained
			res += serializer.inaccessibleArgument();
			exception = true;
		}
		if (!exception) {
			res += serializer.methodArgumentsValuesStart();
			// doing the first iteration separately because the logging potentially need
			// to know if we are at the first element or not to join with a special
			// character
			if (argumentsValueIterator.hasNext()) {
				res += extractAnArgument(argumentsValueIterator);
			}

			while (argumentsValueIterator.hasNext()) {
				res += serializer.joinElementListing();
				res += extractAnArgument(argumentsValueIterator);
			}
			res += serializer.methodArgumentsValuesEnd();
		}
		res += serializer.methodArgumentsEnd();
		return res;
	}

	/**
	 * 
	 * @param argumentsValueIterator the iterator on the arguments
	 */
	private String extractAnArgument(Iterator<Value> argumentsValueIterator) {
		// Here we suppose that method.argumentTypeNames() and frame.getArgumentValues()
		// have the same numbers of items
		// With this supposition being always true, we can just check if one have next
		// and iterate in both
		return extractValueRecursive(argumentsValueIterator.next(), 0);
	}

	/**
	 * Extracting the receiver of this frame
	 * 
	 * @param frame the frame to extract
	 */
	public String extractReceiver(StackFrame frame) {
		String res = "";
		res += serializer.methodReceiverStart();
		res += extractValueRecursive(frame.thisObject(), 0);
		res += serializer.methodReceiverEnd();

		return res;
	}

	/**
	 * extract the given value recursively to make sure no information is lost in
	 * the process
	 * 
	 * @param value the value to extract
	 */
	private String extractValueRecursive(Value value, int depth) {
		String res = "";
		if (maxDepth >= 0 & depth > maxDepth) {
			res += serializer.maxDepth();
		} else if (value == null) {
			res += serializer.nullValue();
		} else if (value instanceof PrimitiveValue) {
			res += extractPrimitiveValue((PrimitiveValue) value, depth);
		} else if (value instanceof ObjectReference) {
			res += extractObjectReference((ObjectReference) value, depth);
		} else {
			// in case there would be another type
			throw new IllegalStateException(
					"Unknown Value Type: " + value.type().name() + ", parsing not yet implemented for this type");
		}
		return res;
	}

	/**
	 * extract given the primitive value
	 * 
	 * @param value the primitiveValue to extract
	 */
	private String extractPrimitiveValue(PrimitiveValue value, int depth) {
		return serializer.primitiveValue(value);
	}

	/**
	 * extract the given ObjectReference
	 * 
	 * @param value the ObjectReference to extract
	 */
	private String extractObjectReference(ObjectReference value, int depth) {
		String res = "";

		res += serializer.objectReferenceStart();
		if (visited.contains(value)) {
			res += serializer.objectReferenceAlreadyFound(value);
		} else {
			visited.add(value);
			res += serializer.objectReferenceInfoStart(value);
			if (value instanceof StringReference) {
				res += serializer.stringReference((StringReference) value);
			} else if (value instanceof ArrayReference) {

				res += serializer.arrayReferenceStart();
				// Parsing every value of the array
				List<Value> arrayValues = ((ArrayReference) value).getValues();
				if (arrayValues.isEmpty()) {
					res += serializer.emptyArray();
				} else if (maxDepth >= 0 & depth + 1 > maxDepth) {
					// in case the max depth will be attained stop here to not make an array full of
					// maxDepth messages
					res += serializer.maxDepth();
				} else {
					// doing the first iteration separately because the logging potentially need
					// to know if we are at the first element or not to join with a special
					// character
					res += extractArrayValue(depth, arrayValues, 0);

					for (int i = 1; i < arrayValues.size(); i++) {
						res += serializer.joinElementListing();
						res += extractArrayValue(depth, arrayValues, i);
					}

				}
				res += serializer.arrayReferenceEnd();
			} else if (value instanceof ClassObjectReference) {
				// using reflectedType because it is said to be more precise than referenceType
				res += extractAllFields(value, ((ClassObjectReference) value).reflectedType(), depth);

			} else {
				res += extractAllFields(value, value.referenceType(), depth);
			}
			res += serializer.objectReferenceInfoEnd();
		}
		res += serializer.objectReferenceEnd();

		return res;
	}

	/**
	 * Extract one value of the array
	 * 
	 * @param depth       the current depth of the recursion
	 * @param arrayValues the values of the array
	 * @param index       the index of the value to extract
	 */
	private String extractArrayValue(int depth, List<Value> arrayValues, int index) {
		String res = "";
		res += serializer.arrayValueStart(index);
		res += extractValueRecursive(arrayValues.get(index), depth + 1);
		res += serializer.arrayValueEnd();
		return res;
	}

	/**
	 * extract all the fields of an ObjectReference
	 * 
	 * @param ref  the ObjectReference having the fields to extract
	 * @param type the reference type of the ObjectReference
	 */
	private String extractAllFields(ObjectReference ref, ReferenceType type, int depth) {
		String res = "";
		// Check if the class is prepared, if not trying to get any field will throw an
		// exception
		// If the class didn't load it mean it's not useful in the context of this call
		// stack

		if (!type.isPrepared()) {
			// Preparation involves creating the static fields for a class or interface and
			// initializing such fields to their default values

			res += serializer.classNotPrepared();
		} else {
			res += serializer.fieldsStart();
			Iterator<Field> iterator = type.allFields().iterator();
			// doing the first iteration separately because the logging potentially need
			// to know if we are at the first element or not to join with a special
			// character
			if (iterator.hasNext()) {
				res += extractField(ref, depth, iterator.next());
			}

			while (iterator.hasNext()) {
				res += serializer.joinElementListing();
				res += extractField(ref, depth, iterator.next());

			}
			res += serializer.fieldsEnd();
		}
		return res;
	}

	/**
	 * Extract one field of an Object reference
	 * 
	 * @param ref   the ObjectReference where the field is
	 * @param depth the depth of the current recursion
	 * @param field the field to extract
	 */
	private String extractField(ObjectReference ref, int depth, Field field) {
		String res = "";
		boolean exception = false;
		Value fieldValue = null;

		res += serializer.fieldStart(field.name());
		try {
			// TODO
			// We actually extract the static and final fields, should we?
			// it's potential information but could also be noise
			fieldValue = ref.getValue(field);

		} catch (IllegalArgumentException e) {
			res += serializer.inaccessibleField();
			exception = true;
		}
		if (!exception) {

			res += serializer.fieldValueStart();

			res += extractValueRecursive(fieldValue, depth + 1);

			res += serializer.fieldValueEnd();
		}
		res += serializer.fieldEnd();
		return res;
	}

	public void push(StackFrame frame) {
		this.serializedFrames.push(this.extract(frame));
	}

	public void pop() {
		this.serializedFrames.pop();
	}

	public int size() {
		return serializedFrames.size();
	}

	public void writeAll() throws IOException {
		int index = 0;
		ListIterator<String> it = serializedFrames.listIterator();
		FileWriter output = new FileWriter(
				this.loggingConfig.getOutputName() + "." + this.loggingConfig.getExtension());

		output.write(serializer.framesStart());

		while (it.hasNext()) {
			if (index > 0) {
				output.write(serializer.joinElementListing());
			}
			output.write(it.next());
			index += 1;
		}

		output.write(serializer.framesEnd());

		output.close();
	}

}
