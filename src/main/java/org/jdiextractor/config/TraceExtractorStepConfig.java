package org.jdiextractor.config;

import com.fasterxml.jackson.databind.JsonNode;

public class TraceExtractorStepConfig extends AbstractExtractorConfig {

	protected boolean activateEndpoint;
	protected boolean collectValues;
	protected int maxMethodDepth;
	protected int maxSteps;

	public static TraceExtractorStepConfig fromJson(JsonNode rootNode) {
		TraceExtractorStepConfig config = new TraceExtractorStepConfig();
		config.fillFromJson(rootNode);
		return config;
	}

	@Override
	protected void fillFromJson(JsonNode rootNode) {
		super.fillFromJson(rootNode);
		this.activateEndpoint = rootNode.get("activateEndpoint").asBoolean();
		this.collectValues = rootNode.get("collectValues").asBoolean();
		this.maxMethodDepth = rootNode.get("maxMethodDepth").asInt();
		this.maxSteps = rootNode.get("maxSteps").asInt();
	}

	public boolean activateEndpoint() {
		return this.activateEndpoint;
	}
	
	public boolean collectValues() {
		return this.collectValues;
	}
	
	public int getMaxMethodDepth() {
		return this.maxMethodDepth;
	}

	public int getMaxSteps() {
		return this.maxSteps;
	}
}
