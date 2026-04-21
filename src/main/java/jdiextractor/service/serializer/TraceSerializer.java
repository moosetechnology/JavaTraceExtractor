package jdiextractor.service.serializer;

import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceArgument;
import jdiextractor.tracemodel.entities.TraceAssignation;
import jdiextractor.tracemodel.entities.TraceAssignationLeft;
import jdiextractor.tracemodel.entities.TraceAssignationRight;
import jdiextractor.tracemodel.entities.TraceElement;
import jdiextractor.tracemodel.entities.TraceInvocation;
import jdiextractor.tracemodel.entities.TraceMethod;
import jdiextractor.tracemodel.entities.TraceParameter;
import jdiextractor.tracemodel.entities.TraceReceiver;
import jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import jdiextractor.tracemodel.entities.javaType.TraceJavaPrimitiveType;
import jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import jdiextractor.tracemodel.entities.traceValues.TraceField;
import jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

public abstract class TraceSerializer {

	public abstract void startSerialize();

	public abstract void serialize(TraceElement element);

	public abstract void endSerialize();

	public abstract void serialize(Trace trace);

	public abstract void serialize(TraceArgument traceArgument);

	public abstract void serialize(TraceAssignation traceAssignation);

	public abstract void serialize(TraceAssignationLeft traceAssignationLeft);

	public abstract void serialize(TraceInvocation traceInvocation);

	public abstract void serialize(TraceMethod traceMethod);

	public abstract void serialize(TraceReceiver traceReceiver);

	public abstract void serialize(TraceAssignationRight traceAssignationRight);

	public abstract void serialize(TraceParameter traceParameter);

	public abstract void serialize(TracePrimitiveValue tracePrimitiveValue);

	public abstract void serialize(TraceValueAlreadyFound traceValueAlreadyFound);

	public abstract void serialize(TraceArrayReference traceArrayReference);

	public abstract void serialize(TraceArrayValue traceArrayValue);

	public abstract void serialize(TraceClassReference traceClassReference);

	public abstract void serialize(TraceField traceField);

	public abstract void serialize(TraceStringReference traceStringReference);

	public abstract void serialize(TraceJavaPrimitiveType traceJavaPrimitiveType);

	public abstract void serialize(TraceJavaClass traceJavaClass);

}
