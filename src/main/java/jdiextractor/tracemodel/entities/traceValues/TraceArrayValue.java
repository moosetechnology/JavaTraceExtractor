package jdiextractor.tracemodel.entities.traceValues;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;
import jdiextractor.tracemodel.entities.TraceValue;
import jdiextractor.tracemodel.entities.TraceValueContainer;

public class TraceArrayValue extends TraceEntity implements TraceValueContainer {

	private TraceValue value;

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
