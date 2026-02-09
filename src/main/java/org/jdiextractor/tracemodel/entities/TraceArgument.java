package org.jdiextractor.tracemodel.entities;

import org.jdiextractor.service.serializer.TraceSerializer;
import org.jdiextractor.tracemodel.TraceEntity;

/**
 * Argument given to a {@link TraceMethod} during execution
 */
public class TraceArgument extends TraceEntity implements TraceValueContainer {

	private TraceMethod method;

	private TraceValue value;

	public TraceArgument() {

	}

	public void setMethod(TraceMethod method) {
		this.method = method;
	}

	public TraceMethod getMethod() {
		return this.method;
	}

	@Override
	public TraceValue getValue() {
		return value;
	}

	@Override
	public void setValue(TraceValue value) {
		this.value = value;

	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {

		serializer.serialize(this);
	}

}
