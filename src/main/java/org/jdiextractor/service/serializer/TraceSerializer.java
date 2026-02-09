package org.jdiextractor.service.serializer;

import org.jdiextractor.tracemodel.entities.Trace;
import org.jdiextractor.tracemodel.entities.TraceArgument;
import org.jdiextractor.tracemodel.entities.TraceAssignation;
import org.jdiextractor.tracemodel.entities.TraceAssignationLeft;
import org.jdiextractor.tracemodel.entities.TraceAssignationRight;
import org.jdiextractor.tracemodel.entities.TraceInvocation;
import org.jdiextractor.tracemodel.entities.TraceMethod;
import org.jdiextractor.tracemodel.entities.TraceReceiver;

public abstract class TraceSerializer {

	public abstract void serialize(TraceArgument traceArgument);

	public abstract void serialize(TraceAssignation traceAssignation);

	public abstract void serialize(Trace trace);

	public abstract void serialize(TraceAssignationLeft traceAssignationLeft);

	public abstract void serialize(TraceInvocation traceInvocation);

	public abstract void serialize(TraceMethod traceMethod);

	public abstract void serialize(TraceReceiver traceReceiver);

	public abstract void serialize(TraceAssignationRight traceAssignationRight);

}
