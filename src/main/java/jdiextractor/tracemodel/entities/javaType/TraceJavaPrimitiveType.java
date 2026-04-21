package jdiextractor.tracemodel.entities.javaType;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceJavaPrimitiveType extends TraceJavaType {

	public TraceJavaPrimitiveType(String name) {
		super(name);
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
