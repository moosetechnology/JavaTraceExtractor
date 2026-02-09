package org.jdiextractor.tracemodel.entities;

import org.jdiextractor.service.serializer.TraceSerializer;
import org.jdiextractor.tracemodel.TraceEntity;

public class TraceReceiver extends TraceEntity implements TraceValueContainer {

	private TraceMethod method;

	private TraceValue value;

	public TraceReceiver() {

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
