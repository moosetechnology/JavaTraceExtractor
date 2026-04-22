package jdiextractor.service.serializer;

import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceElement;

/**
 * A Trace converter that retain TraceElements and serialize the Trace only at the end
 * It enable the analysis and verification of obtained Traces directly in java
 */
public class DefferedTraceConverter extends JDIToTraceConverter {

	private Trace trace;

	public DefferedTraceConverter(boolean valuesIndependents, int maxObjectDepth, TraceLogger logger) {
		super(valuesIndependents, maxObjectDepth, logger);
		this.trace = new Trace();
	}

	@Override
	protected void addElement(TraceElement element) {
		this.trace.addElement(element);
	}

	@Override
	public void serialize() {
		logger.serialize(trace);
	}

	@Override
	public void removeLastElement() {
		this.trace.removeLastElement();
	}

	@Override
	public Trace getTrace() {
		return this.trace;
	}

}
