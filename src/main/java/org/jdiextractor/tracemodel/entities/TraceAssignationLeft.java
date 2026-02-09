package org.jdiextractor.tracemodel.entities;

import org.jdiextractor.service.serializer.TraceSerializer;
import org.jdiextractor.tracemodel.TraceEntity;

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
