package jdiextractor.tracemodel.entities;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;

public class TraceAssignationLeft extends TraceEntity implements TraceValueContainer {

	private TraceValue value;

	public TraceAssignationLeft() {

	}

	public TraceValue getValue() {
		return value;
	}

	public void setValue(TraceValue value) {
		this.value = value;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
