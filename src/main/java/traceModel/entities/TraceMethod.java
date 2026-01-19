package traceModel.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific method execution call in the trace.
 */
public class TraceMethod extends TraceElement {

	private Invocation invocation = null;

	private Receiver receiver = null;

	private List<Argument> arguments = new ArrayList<>();

	public TraceMethod() {

	}

	public Invocation getInvocation() {
		return invocation;
	}

	public void setInvocation(Invocation invocation) {
		this.invocation = invocation;
	}

	public Receiver getReceiver() {
		return receiver;
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	public List<Argument> getArguments() {
		return arguments;
	}

	public void setArguments(List<Argument> arguments) {
		this.arguments = arguments;
	}

	public void addArgument(Argument argument) {
		this.arguments.add(argument);
	}

}