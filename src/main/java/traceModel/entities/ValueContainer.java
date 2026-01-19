package traceModel.entities;

/**
 * Container that holds a specific {@link TraceValue}.
 */
public interface ValueContainer {

    public TraceValue getValue();

    public void setValue(TraceValue value);

}
