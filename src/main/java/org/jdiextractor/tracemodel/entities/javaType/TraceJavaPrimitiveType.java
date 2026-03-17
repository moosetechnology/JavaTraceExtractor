package org.jdiextractor.tracemodel.entities.javaType;

import org.jdiextractor.service.serializer.TraceSerializer;

public class TraceJavaPrimitiveType extends TraceJavaType {

	public TraceJavaPrimitiveType(String name) {
		super(name);
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
