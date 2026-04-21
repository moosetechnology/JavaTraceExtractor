package jdiextractor.tracemodel.entities.traceValues;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;
import jdiextractor.tracemodel.entities.TraceValue;
import jdiextractor.tracemodel.entities.TraceValueContainer;

public class TraceField extends TraceEntity implements TraceValueContainer {

	private boolean isAccessible = true;

	private boolean isAtMaxDepth = false;

	private String name;

	private TraceValue value;

	public boolean isAccessible() {
		return isAccessible;
	}

	public void setAccessible(boolean isAccessible) {
		this.isAccessible = isAccessible;
	}

	public boolean isAtMaxDepth() {
		return isAtMaxDepth;
	}

	public void setAtMaxDepth(boolean isAtMaxDepth) {
		this.isAtMaxDepth = isAtMaxDepth;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
