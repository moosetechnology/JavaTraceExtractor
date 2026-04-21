package jdiextractor.tracemodel.entities;

/**
 * Container that holds a specific {@link TraceValue}.
 */
public interface TraceValueContainer {

    TraceValue getValue();

    void setValue(TraceValue value);

}
