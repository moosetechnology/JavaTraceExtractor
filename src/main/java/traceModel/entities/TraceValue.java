package traceModel.entities;

import com.sun.jdi.Value;

import traceModel.TraceEntity;

/**
 * Abstract representation of a runtime value.
 */
public abstract class TraceValue extends TraceEntity {

	private Value value;

	public TraceValue() {

	}

	public void setValue(Value value) {
		this.value = value;
	}

	public Value getValue() {
		return value;
	}
}
