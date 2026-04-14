package org.jdiextractor.service.serializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdiextractor.tracemodel.entities.TraceArgument;
import org.jdiextractor.tracemodel.entities.TraceElement;
import org.jdiextractor.tracemodel.entities.TraceMethod;
import org.jdiextractor.tracemodel.entities.TraceParameter;
import org.jdiextractor.tracemodel.entities.TraceReceiver;
import org.jdiextractor.tracemodel.entities.TraceValue;
import org.jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import org.jdiextractor.tracemodel.entities.javaType.TraceJavaPrimitiveType;
import org.jdiextractor.tracemodel.entities.javaType.TraceJavaType;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceField;
import org.jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public abstract class JDIToTraceConverter {

	/**
	 * whether the values are independents with each other, if they are, visited is
	 * reseted between each between each TraceElement creation
	 */
	private boolean valuesIndependents;

	/**
	 * The maximum depth of the object graph
	 */
	private int maxObjectDepth;

	/**
	 * The logger used when the trace population is finished
	 */
	protected TraceLogger logger;

	/**
	 * All already visited object references
	 */
	private Set<Long> visitedIds = new HashSet<>();

	public JDIToTraceConverter(boolean valuesIndependents, int maxObjectDepth, TraceLogger logger) {
		this.valuesIndependents = valuesIndependents;
		this.maxObjectDepth = maxObjectDepth;
		this.logger = logger;
	}

	protected abstract void addElement(TraceElement element);

	public abstract void serialize();

	public abstract void removeLastElement();

	public TraceMethod newMethodFrom(Method method, List<Value> argumentValues, ObjectReference receiverObject) {
		TraceMethod traceMethod = this.coreNewMethodFrom(method);

		if (argumentValues == null) {
			traceMethod.setArgumentAccessible(false);
		} else {
			Iterator<TraceArgument> ite = this.createArgumentsFor(argumentValues).iterator();
			while (ite.hasNext()) {
				traceMethod.addArgument(ite.next());
			}
		}

		if (receiverObject == null) {
			traceMethod.setReceiver(null);
		} else {

			traceMethod.setReceiver(this.newReceiverFrom(receiverObject));
		}

		this.addElement(traceMethod);
		return traceMethod;
	}

	public TraceMethod newMethodFrom(Method method) {
		TraceMethod traceMethod = this.coreNewMethodFrom(method);

		this.addElement(traceMethod);
		return traceMethod;
	}

	private TraceMethod coreNewMethodFrom(Method method) {
		if (valuesIndependents) {
			visitedIds = new HashSet<>();
		}

		TraceMethod traceMethod = new TraceMethod();
		traceMethod.setName(method.name());
		traceMethod.setSignature(this.signatureParameter(method));
		traceMethod.setClassSide(method.isStatic());
		traceMethod.setParentType(this.newJavaClassFrom(method.location().declaringType()));
		Iterator<TraceParameter> ite = this.createParametersFor(method).iterator();
		while (ite.hasNext()) {
			traceMethod.addParameter(ite.next());
		}

		// A method with a generericSignature is a parametric method
		// Note we could parse the signature to know the real arguments
		if (method.genericSignature() != null) {
			traceMethod.setIsParametric(true);
		}
		return traceMethod;
	}

	private TraceJavaType newJavaTypeFrom(Type type) {
		if (type instanceof ReferenceType) {
			return newJavaClassFrom((ReferenceType) type);
		} else {
			return new TraceJavaPrimitiveType(type.name());
		}
	}

	/**
	 * Create a JavaType from a String Pay Attention : this method is not able to
	 * determine whether a class is parametric or not
	 * 
	 * @param typeName the name of the JavaType to create
	 * @return the JavaType created
	 */
	private TraceJavaType newJavaTypeFrom(String typeName) {
		if (typeName.contains(".") || Character.isUpperCase(typeName.charAt(0))) {
			return new TraceJavaClass(typeName);
		} else {
			return new TraceJavaPrimitiveType(typeName);
		}
	}

	private TraceJavaClass newJavaClassFrom(ReferenceType declaringType) {
		TraceJavaClass traceJavaClass = new TraceJavaClass(declaringType.name());

		if (declaringType.genericSignature() != null) {
			traceJavaClass.setIsParametric(true);
		}
		return traceJavaClass;
	}

	private TraceReceiver newReceiverFrom(ObjectReference receiverObject) {
		TraceReceiver traceReceiver = new TraceReceiver();
		traceReceiver.setValue(this.newValueFromObjectReference(receiverObject, 0));

		return traceReceiver;
	}

	private List<TraceArgument> createArgumentsFor(List<Value> argumentValues) {
		List<TraceArgument> res = new ArrayList<>();
		Iterator<Value> argumentsValueIterator = argumentValues.iterator();

		while (argumentsValueIterator.hasNext()) {
			res.add(this.newArgumentFrom(argumentsValueIterator.next()));
		}
		return res;
	}

	private TraceArgument newArgumentFrom(Value value) {
		TraceArgument traceArgument = new TraceArgument();
		traceArgument.setValue(this.newValueFrom(value, 0));
		return traceArgument;
	}

	private List<TraceParameter> createParametersFor(Method method) {
		List<TraceParameter> res = new ArrayList<>();
		try {
			// trying to obtain the arguments information
			Iterator<LocalVariable> ite = method.arguments().iterator();

			while (ite.hasNext()) {
				res.add(newParameterFrom(ite.next()));
			}

		} catch (AbsentInformationException e) {
			// arguments name could not be obtained
			// Since the name are not obtainable just log the parameters types
			// This happen when classes are not yet loaded or with native methods
			Iterator<String> ite = method.argumentTypeNames().iterator();
			while (ite.hasNext()) {
				res.add(this.newParameterFrom(ite.next()));
			}
		}

		return res;
	}

	private TraceParameter newParameterFrom(LocalVariable param) {
		TraceParameter traceParameter = new TraceParameter();
		traceParameter.setName(param.name());
		try {
			traceParameter.setType(this.newJavaTypeFrom(param.type()));
		} catch (ClassNotLoadedException e) {
			throw new RuntimeException("Should not happen");
		}
		return traceParameter;
	}

	private TraceParameter newParameterFrom(String typeName) {
		TraceParameter traceParameter = new TraceParameter();
		traceParameter.setName(null);
		traceParameter.setType(this.newJavaTypeFrom(typeName));
		return traceParameter;
	}

	private TraceValue newValueFrom(Value value, int depth) {
		TraceValue traceValue;
		if (value == null) {
			traceValue = null;
		} else if (value instanceof PrimitiveValue) {
			traceValue = newValueFromPrimitiveValue((PrimitiveValue) value, depth);
		} else if (value instanceof ObjectReference) {
			traceValue = newValueFromObjectReference((ObjectReference) value, depth);
		} else {
			// in case there would be another type, should not happen
			throw new IllegalStateException(
					"Unknown Value Type: " + value.type().name() + ", parsing not yet implemented for this type");
		}
		return traceValue;
	}

	private TraceValue newValueFromPrimitiveValue(PrimitiveValue value, int depth) {
		TracePrimitiveValue tracePrimitiveValue = new TracePrimitiveValue();
		tracePrimitiveValue.setType(value.type().name());
		tracePrimitiveValue.setValue(value);
		return tracePrimitiveValue;
	}

	private TraceValue newValueFromObjectReference(ObjectReference objectReference, int depth) {
		long id = objectReference.uniqueID();

		if (visitedIds.contains(id)) {
			return new TraceValueAlreadyFound(id);
		} else {
			visitedIds.add(id);
			if (objectReference instanceof StringReference) {
				return this.newStringReferenceFrom((StringReference) objectReference);
			} else if (objectReference instanceof ArrayReference) {
				return this.newArrayReferenceFrom((ArrayReference) objectReference, depth);
			} else {
				return this.newClassReferenceFrom(objectReference, objectReference.referenceType(), depth);
			}
		}
	}

	private TraceValue newClassReferenceFrom(ObjectReference ref, ReferenceType type, int depth) {
		TraceClassReference traceClassReference = new TraceClassReference();
		traceClassReference.setUniqueID(ref.uniqueID());
		traceClassReference.setType(this.newJavaClassFrom(type));

		if (!type.isPrepared()) {
			traceClassReference.setPrepared(false);
			return traceClassReference;
		}

		Iterator<Field> iterator = type.allFields().iterator();
		while (iterator.hasNext()) {
			traceClassReference.addField(this.newFieldFrom(ref, depth, iterator.next()));
		}

		return traceClassReference;
	}

	private TraceField newFieldFrom(ObjectReference objectReference, int depth, Field field) {
		TraceField tracefield = new TraceField();
		tracefield.setName(field.name());

		Value fieldValue = null;

		try {
			fieldValue = objectReference.getValue(field);
		} catch (IllegalArgumentException e) {
			tracefield.setAccessible(false);
			return tracefield;
		}
		if (maxObjectDepth >= 0 & depth + 1 > maxObjectDepth) {
			tracefield.setAtMaxDepth(true);
		} else {
			tracefield.setValue(this.newValueFrom(fieldValue, depth + 1));
		}

		return tracefield;
	}

	private TraceArrayReference newArrayReferenceFrom(ArrayReference arrayReference, int depth) {
		TraceArrayReference traceArrayReference = new TraceArrayReference();
		traceArrayReference.setUniqueID(arrayReference.uniqueID());
		traceArrayReference.setType(this.newJavaClassFrom(arrayReference.referenceType()));

		List<Value> arrayValues = arrayReference.getValues();

		if (!arrayValues.isEmpty() & maxObjectDepth >= 0 & depth + 1 > maxObjectDepth) {
			traceArrayReference.setAtMaxDepth(true);
			return traceArrayReference;
		} else {

			for (int i = 0; i < arrayValues.size(); i++) {
				traceArrayReference.addArrayValue(this.newArrayValueFrom(depth, arrayValues, i));
			}

		}
		return traceArrayReference;
	}

	private TraceArrayValue newArrayValueFrom(int depth, List<Value> arrayValues, int index) {
		TraceArrayValue traceArrayValue = new TraceArrayValue();
		traceArrayValue.setValue(this.newValueFrom(arrayValues.get(index), depth + 1));
		return traceArrayValue;
	}

	private TraceValue newStringReferenceFrom(StringReference stringReference) {
		TraceStringReference traceStringReference = new TraceStringReference();
		/* Cannot collect the value of strings because in some attacks, other type are stored instead of String in some variables
		 * Hence we just stop to collect string value for now
		 * see issue https://github.com/moosetechnology/JavaTraceExtractor/issues/25
		 */
		//traceStringReference.setValue(stringReference.value());
		traceStringReference.setUniqueID(stringReference.uniqueID());
		traceStringReference.setType(this.newJavaClassFrom(stringReference.referenceType()));
		return traceStringReference;
	}

	/**
	 * Returns the signature of a method formatted for the Moose model.
	 * * @param method The JDI Method object
	 * @return the signature of the method (e.g., "methodName(Type1,Type2)")
	 */
	private String signatureParameter(Method method) {
		String genericSignature = method.genericSignature();

		// 1. Absolute source of truth: The JNI generic signature
		if (genericSignature != null) {
			int start = genericSignature.indexOf('(');
			int end = genericSignature.lastIndexOf(')');

			if (start != -1 && end != -1) {
				// Extract the parameter section between the parentheses
				String paramsSignature = genericSignature.substring(start + 1, end);
				
				// Fully delegate the lexical parsing to the converter
				JVMSignatureToMooseSignatureConverter parser = JVMSignatureToMooseSignatureConverter.make();
				String parsedParams = parser.parseMethodParameters(paramsSignature);
				
				return String.format("%s(%s)", method.name(), parsedParams);
			}
		}

		// 2. Standard fallback (Type erasure applied, local variables or base types)
		String paramString = "";
		try {
			Iterator<LocalVariable> ite = method.arguments().iterator();
			if (ite.hasNext()) {
				paramString = parseTypeName(ite.next());
			}
			while (ite.hasNext()) {
				paramString = String.format("%s,%s", paramString, parseTypeName(ite.next()));
			}
		} catch (AbsentInformationException e) {
			// In case debug information (local variables) is absent
			Iterator<String> ite = method.argumentTypeNames().iterator();
			if (ite.hasNext()) {
				paramString = parseTypeName(ite.next());
			}
			while (ite.hasNext()) {
				paramString = String.format("%s,%s", paramString, parseTypeName(ite.next()));
			}
		}
		return String.format("%s(%s)", method.name(), paramString);
	}

	private String parseTypeName(LocalVariable var) {
		JVMSignatureToMooseSignatureConverter parser = JVMSignatureToMooseSignatureConverter.make();
		if (var.genericSignature() == null)
			return parser.parseTypeSig(var.signature());
		else
			return parser.parseTypeSig(var.genericSignature());
	}

	private String parseTypeName(String s) {
		int lastDotIndex = s.lastIndexOf('.');
		return lastDotIndex == -1 ? s : s.substring(lastDotIndex + 1);
	}
}
