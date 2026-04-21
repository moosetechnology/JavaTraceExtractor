package jdiextractor.tracemodel.entities;

import jdiextractor.tracemodel.TraceEntity;

/**
 * Abstract base class representing an atomic event in the execution trace.
 */
public abstract class TraceElement extends TraceEntity {

	protected Trace trace = null;
	
	/*
	 * The id of this trace element, this can be used to link trace element that are direct children of this
	 * Do not confuse it with the attribute `uniqueId` of the values that are generated from the JDI 
	 */
	protected int id;
	
	/*
	 * The id of the parent of this element
	 * Note : a negative member mean that the element has no parent, happen for the first method of an execution
	 */
	protected int parentId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public TraceElement() {
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public Trace getTrace() {
		return this.trace;
	}
}
