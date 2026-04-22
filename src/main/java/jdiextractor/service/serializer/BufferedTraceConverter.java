package jdiextractor.service.serializer;

import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceElement;

/**
 * A Trace converter that does not retain TraceElement and automatically serialize the trace
 * It enable the serialization of large trace data without excessive consumption on the computer memory
 */
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

	@Override
	public Trace getTrace() {
		throw new RuntimeException("BufferedTraceConverter cannot give the trace because it does not retain information and automatically log informations");
	}

}
