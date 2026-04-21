package jdiextractor.service.serializer;

import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceElement;

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

}
