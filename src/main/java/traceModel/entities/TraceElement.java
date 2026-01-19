package traceModel.entities;

import traceModel.TraceEntity;

/**
 * Abstract base class representing an atomic event in the execution trace.
 */
public abstract class TraceElement extends TraceEntity {

	protected Trace trace = null;

	public TraceElement() {
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public Trace getTrace() {
		return this.trace;
	}
}
