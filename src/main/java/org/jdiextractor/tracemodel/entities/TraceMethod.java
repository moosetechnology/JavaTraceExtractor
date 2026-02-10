package org.jdiextractor.tracemodel.entities;

import java.util.ArrayList;
import java.util.List;

import org.jdiextractor.service.serializer.TraceSerializer;

/**
 * Represents a specific method execution call in the trace.
 */
public class TraceMethod extends TraceElement {

	private TraceInvocation invocation = null;

	private TraceReceiver receiver = null;

	private List<TraceArgument> arguments = new ArrayList<>();

	private List<TraceParameter> parameters = new ArrayList<>();

	private String name;

	private String signature;

	private boolean isClassSide;

	private boolean isArgsAccessible = true;

	private String parentType;

	public TraceMethod() {

	}

	public TraceInvocation getInvocation() {
		return invocation;
	}

	public void setInvocation(TraceInvocation invocation) {
		this.invocation = invocation;
	}

	public TraceReceiver getReceiver() {
		return receiver;
	}

	public void setReceiver(TraceReceiver receiver) {
		this.receiver = receiver;
	}

	public List<TraceArgument> getArguments() {
		return arguments;
	}

	public void setArguments(List<TraceArgument> arguments) {
		this.arguments = arguments;
	}

	public void addArgument(TraceArgument argument) {
		this.arguments.add(argument);
	}

	public List<TraceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<TraceParameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(TraceParameter parameter) {
		this.parameters.add(parameter);
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return this.signature;
	}

	public void setName(String name) {
		this.name = name;

	}

	public String getName() {
		return this.name;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

	public void setClassSide(boolean isClassSide) {
		this.isClassSide = isClassSide;

	}

	public boolean isClassSide() {
		return this.isClassSide;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getParentType() {
		return this.parentType;
	}

	public void setArgumentAccessible(boolean b) {
		this.isArgsAccessible = b;
	}

	public boolean isArgsAccessible() {
		return this.isArgsAccessible;
	}

}