package jdiextractor.tracemodel.entities;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;

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
