package jdiextractor.service.serializer;

import jdiextractor.tracemodel.entities.TraceElement;

public class BufferedTraceConverter extends JDIToTraceConverter {


	public BufferedTraceConverter(boolean valuesIndependents, int maxObjectDepth, TraceLogger logger) {
		super(valuesIndependents, maxObjectDepth, logger);
		logger.startSerialize();
	}

	@Override
	protected void addElement(TraceElement element) {
		logger.serialize(element);
	}

	@Override
	public void serialize() {
		logger.endSerialize();
	}

	@Override
	public void removeLastElement() {
		throw new IllegalStateException("Should not remove an element on buffered converter");
	}

}
