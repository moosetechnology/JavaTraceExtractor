package org.jdiextractor.tracemodel.entities;

import java.util.ArrayList;
import java.util.List;

import org.jdiextractor.service.serializer.TraceSerializer;
import org.jdiextractor.tracemodel.TraceEntity;

public class Trace extends TraceEntity {

	private List<TraceElement> elements;

	public Trace() {
		this.elements = new ArrayList<>();
	}

	public void addElement(TraceElement element) {
		this.elements.add(element);
		element.setTrace(this);
	}

	public List<TraceElement> getElements() {
		return elements;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
