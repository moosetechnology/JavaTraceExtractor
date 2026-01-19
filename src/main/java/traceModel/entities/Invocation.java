package traceModel.entities;

import com.sun.jdi.Method;

import traceModel.TraceEntity;

/**
 * Representing an Invocation of a method
 */
public class Invocation extends TraceEntity {

	private Method sender;
	private Method invokee;
	
	public Invocation(Method sender, Method invokee) {
		this.sender = sender;
		this.invokee = invokee;
	}

	public Method getSender() {
		return sender;
	}

	public void setSender(Method sender) {
		this.sender = sender;
	}

	public Method getInvokee() {
		return invokee;
	}

	public void setInvokee(Method invokee) {
		this.invokee = invokee;
	}
	
	
	
}
