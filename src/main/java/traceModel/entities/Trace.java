package traceModel.entities;

import java.util.ArrayList;
import java.util.List;

import traceModel.TraceEntity;

public class Trace extends TraceEntity {

	private List<TraceElement> elements;

	public Trace() {
		this.elements = new ArrayList<>();
	}

	public void addElement(TraceElement element) {
		this.elements.add(element);
		element.setTrace(this);
	}

}
