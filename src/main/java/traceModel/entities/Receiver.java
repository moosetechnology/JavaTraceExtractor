package traceModel.entities;

import traceModel.TraceEntity;

public class Receiver extends TraceEntity implements ValueContainer {

	private TraceMethod method;

	private TraceValue value;

	public Receiver() {

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

}
