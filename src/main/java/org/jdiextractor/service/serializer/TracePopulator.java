package org.jdiextractor.service.serializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdiextractor.tracemodel.entities.Trace;
import org.jdiextractor.tracemodel.entities.TraceArgument;
import org.jdiextractor.tracemodel.entities.TraceMethod;
import org.jdiextractor.tracemodel.entities.TraceParameter;
import org.jdiextractor.tracemodel.entities.TraceReceiver;
import org.jdiextractor.tracemodel.entities.TraceValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassNotPrepared;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceField;
import org.jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceValueMaxDepth;
import org.jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

public class TracePopulator {

	private Trace trace;

	/**
	 * whether the values are independents with each other, if they are, visited is
	 * reseted between each between each TraceElement creation
	 * 
	 * By default true
	 */
	private boolean valuesIndependents = true;

	/**
	 * The maximum depth of the object graph
	 */
	private int maxDepth;

	/**
	 * All already visited object references
	 */
	private Set<Long> visitedIds = new HashSet<>();

	public TracePopulator(boolean valuesIndependents, int maxDepth) {
		this.trace = new Trace();
		this.valuesIndependents = valuesIndependents;
		this.maxDepth = maxDepth;
	}

	public TracePopulator(int maxDepth) {
		this.trace = new Trace();
		this.maxDepth = maxDepth;
	}

	public Trace getTrace() {
		return this.trace;
	}

	public TraceMethod newMethodFrom(Method method, List<Value> argumentValues, ObjectReference receiverObject) {
		TraceMethod traceMethod = this.newMethodFrom(method);

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

		return traceMethod;
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

	public TraceMethod newMethodFrom(Method method) {
		if (valuesIndependents) {
			visitedIds = new HashSet<>();
		}

		TraceMethod traceMethod = new TraceMethod();
		traceMethod.setName(method.name());
		traceMethod.setSignature(this.signatureParameter(method));
		traceMethod.setClassSide(method.isStatic());
		traceMethod.setParentType(this.parentType(method));
		Iterator<TraceParameter> ite = this.createParametersFor(method).iterator();
		while (ite.hasNext()) {
			traceMethod.addParameter(ite.next());
		}

		this.trace.addElement(traceMethod);
		return traceMethod;
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
		traceParameter.setTypeName(param.typeName());
		return traceParameter;
	}

	private TraceParameter newParameterFrom(String typeName) {
		TraceParameter traceParameter = new TraceParameter();
		traceParameter.setName(null);
		traceParameter.setTypeName(typeName);
		return traceParameter;
	}

	private TraceValue newValueFrom(Value value, int depth) {
		TraceValue traceValue;
		if (maxDepth >= 0 & depth > maxDepth) {
			traceValue = new TraceValueMaxDepth();
		} else if (value == null) {
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
			} else if (objectReference instanceof ClassObjectReference) {
				return this.newClassReferenceFrom(objectReference,
						((ClassObjectReference) objectReference).reflectedType(), depth);
			} else {
				return this.newClassReferenceFrom(objectReference, objectReference.referenceType(), depth);
			}
		}
	}

	private TraceValue newClassReferenceFrom(ObjectReference ref, ReferenceType type, int depth) {

		if (!type.isPrepared()) {
			TraceClassNotPrepared traceClassNotPrepared = new TraceClassNotPrepared();
			traceClassNotPrepared.setUniqueID(ref.uniqueID());
			traceClassNotPrepared.setType(ref.referenceType().name());
			return traceClassNotPrepared;
		}
		TraceClassReference traceClassReference = new TraceClassReference();
		traceClassReference.setUniqueID(ref.uniqueID());
		traceClassReference.setType(ref.referenceType().name());

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

		tracefield.setValue(this.newValueFrom(fieldValue, depth + 1));

		return tracefield;
	}

	private TraceArrayReference newArrayReferenceFrom(ArrayReference arrayReference, int depth) {
		TraceArrayReference traceArrayReference = new TraceArrayReference();
		traceArrayReference.setUniqueID(arrayReference.uniqueID());
		traceArrayReference.setType(arrayReference.referenceType().name());

		List<Value> arrayValues = arrayReference.getValues();

		if (!arrayValues.isEmpty() & maxDepth >= 0 & depth + 1 > maxDepth) {
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
		traceStringReference.setValue(stringReference.value());
		traceStringReference.setUniqueID(stringReference.uniqueID());
		traceStringReference.setType(stringReference.referenceType().name());
		return traceStringReference;
	}

	/**
	 * --------------------------------------- UTILS
	 * ---------------------------------------
	 */

	/**
	 * Adapt the parent type of the method to make sure it matches moose parent
	 * types
	 * 
	 * @param method
	 * @return the parent type of the method
	 */
	public String parentType(Method method) {
		String parentType = method.location().declaringType().name();
		if (parentType.contains(".")) {
			return parentType;
		}

		return "<Default Package>.".concat(parentType);
	}

	public String signatureParameter(Method method) {
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
			// nothing to do
		}
		return String.format("%s(%s)", method.name(), paramString);
	}

	public String parseTypeName(LocalVariable var) {
		JVMSignatureToMooseSignatureConverter parser = JVMSignatureToMooseSignatureConverter.make();
		if (var.genericSignature() == null)
			return parser.parseTypeSig(var.signature());
		else
			return parser.parseTypeSig(var.genericSignature());
	}

}
