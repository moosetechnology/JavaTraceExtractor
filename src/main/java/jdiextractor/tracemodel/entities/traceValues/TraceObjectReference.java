package jdiextractor.tracemodel.entities.traceValues;

import jdiextractor.tracemodel.entities.TraceValue;
import jdiextractor.tracemodel.entities.javaType.TraceJavaType;

public abstract class TraceObjectReference extends TraceValue {

	protected long uniqueID;

	private TraceJavaType type;

	public long getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(long uniqueID) {
		this.uniqueID = uniqueID;
	}

	public void setType(TraceJavaType type) {
		this.type = type;
	}

	public TraceJavaType getType() {
		return this.type;
	}
}
