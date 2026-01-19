package traceModel.entities;

import traceModel.TraceEntity;

public class AssignationLeft extends TraceEntity implements ValueContainer {

	private TraceValue value;

	public AssignationLeft() {

	}

	public TraceValue getValue() {
		return value;
	}

	public void setValue(TraceValue value) {
		this.value = value;
	}

}
