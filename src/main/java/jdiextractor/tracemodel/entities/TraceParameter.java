package jdiextractor.tracemodel.entities;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;
import jdiextractor.tracemodel.entities.javaType.TraceJavaType;

public class TraceParameter extends TraceEntity {

	private String name;

	private TraceJavaType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TraceJavaType getType() {
		return type;
	}

	public void setType(TraceJavaType type) {
		this.type = type;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
