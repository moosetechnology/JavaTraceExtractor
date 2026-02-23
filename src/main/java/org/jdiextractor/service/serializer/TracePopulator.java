package org.jdiextractor.service.serializer;

import org.jdiextractor.tracemodel.entities.Trace;
import org.jdiextractor.tracemodel.entities.TraceElement;

public class TracePopulator extends JDIToTraceConverter {

	private Trace trace;

	public TracePopulator(boolean valuesIndependents, int maxObjectDepth, TraceLogger logger) {
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
