package jdiextractor.tracemodel.entities;

import java.util.List;
import java.util.Stack;

import jdiextractor.service.serializer.TraceSerializer;
import jdiextractor.tracemodel.TraceEntity;

public class Trace extends TraceEntity {

	private Stack<TraceElement> elements;

	public Trace() {
		this.elements = new Stack<>();
	}

	public void addElement(TraceElement element) {
		this.elements.push(element);
		element.setTrace(this);
	}

	public TraceElement removeLastElement() {
		return this.elements.pop();
	}

	public List<TraceElement> getElements() {
		return elements;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

	public int size() {
		return elements.size();
	}

}
