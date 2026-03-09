package org.jdiextractor.service.serializer;

import org.jdiextractor.tracemodel.entities.Trace;
import org.jdiextractor.tracemodel.entities.TraceArgument;
import org.jdiextractor.tracemodel.entities.TraceAssignation;
import org.jdiextractor.tracemodel.entities.TraceAssignationLeft;
import org.jdiextractor.tracemodel.entities.TraceAssignationRight;
import org.jdiextractor.tracemodel.entities.TraceElement;
import org.jdiextractor.tracemodel.entities.TraceInvocation;
import org.jdiextractor.tracemodel.entities.TraceMethod;
import org.jdiextractor.tracemodel.entities.TraceParameter;
import org.jdiextractor.tracemodel.entities.TraceReceiver;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassNotPrepared;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceField;
import org.jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

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

	public abstract void serialize(TraceClassNotPrepared traceClassNotPrepared);

	public abstract void serialize(TraceClassReference traceClassReference);

	public abstract void serialize(TraceField traceField);

	public abstract void serialize(TraceStringReference traceStringReference);

}
