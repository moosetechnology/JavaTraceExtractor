package jdiextractor.tracemodel.entities.javaType;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceJavaInterface extends TraceJavaReferenceType {

	public TraceJavaInterface(String name) {
		super(name);
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
