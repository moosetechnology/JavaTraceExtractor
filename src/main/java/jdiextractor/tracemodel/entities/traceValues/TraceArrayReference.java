package jdiextractor.tracemodel.entities.traceValues;

import java.util.ArrayList;
import java.util.List;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceArrayReference extends TraceObjectReference {

	private List<TraceArrayValue> arrayValues = new ArrayList<>();


	private boolean isAtMaxDepth = false;

	public void setArrayValues(List<TraceArrayValue> arrayValues) {
		this.arrayValues = arrayValues;
	}

	public List<TraceArrayValue> getArrayValues() {
		return this.arrayValues;
	}

	public void addArrayValue(TraceArrayValue traceArrayValue) {
		this.arrayValues.add(traceArrayValue);

	}

	public boolean isAtMaxDepth() {
		return isAtMaxDepth;
	}

	public void setAtMaxDepth(boolean isAtMaxDepth) {
		this.isAtMaxDepth = isAtMaxDepth;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}


}
