package jdiextractor.tracemodel.entities.traceValues;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceValueAlreadyFound extends TraceObjectReference {


	public TraceValueAlreadyFound(long uniqueID) {
		this.uniqueID = uniqueID;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);

	}

}
