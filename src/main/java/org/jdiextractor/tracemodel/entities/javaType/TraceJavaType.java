package org.jdiextractor.tracemodel.entities.javaType;

import org.jdiextractor.tracemodel.TraceEntity;

public abstract class TraceJavaType extends TraceEntity {

	private String name;

	public TraceJavaType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
