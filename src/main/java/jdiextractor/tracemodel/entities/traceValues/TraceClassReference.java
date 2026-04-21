package jdiextractor.tracemodel.entities.traceValues;

import java.util.ArrayList;
import java.util.List;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceClassReference extends TraceObjectReference {

	private List<TraceField> fields = new ArrayList<>();

	private boolean isPrepared = true;
	
	public boolean isPrepared() {
		return isPrepared;
	}

	public void setPrepared(boolean isPrepared) {
		this.isPrepared = isPrepared;
	}

	public void setFields(List<TraceField> fields) {
		this.fields = fields;
	}

	public List<TraceField> getFields() {
		return this.fields;
	}

	public void addField(TraceField traceField) {
		this.fields.add(traceField);

	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
