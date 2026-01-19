package traceModel.entities;

import traceModel.TraceEntity;

/**
 * Argument given to a {@link TraceMethod} during execution
 */
public class Argument extends TraceEntity implements ValueContainer {

	private TraceMethod method;

	private TraceValue value;

	public Argument() {

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
