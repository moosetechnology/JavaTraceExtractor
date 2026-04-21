package jdiextractor.tracemodel.entities.javaType;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceJavaClass extends TraceJavaType {

	private boolean isParametric;

	public TraceJavaClass(String name) {
		super(name);
	}

	public boolean isParametric() {
		return isParametric;
	}

	public void setIsParametric(boolean isParametric) {
		this.isParametric = isParametric;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
