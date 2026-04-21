package jdiextractor.config;

import com.fasterxml.jackson.databind.JsonNode;

import jdiextractor.config.builder.TraceExtractorStepConfigBuilder;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.config.components.LoggingConfig;
import jdiextractor.config.components.VmConfig;

public class TraceExtractorStepConfig extends AbstractExtractorConfig {

	public final static boolean DEFAULT_ACTIVATE_ENDPOINT = false;
	public final static boolean DEFAULT_COLLECT_VALUES = true;
	public final static int DEFAULT_MAX_METHOD_DEPTH = 14;
	public final static int DEFAULT_MAX_STEPS = 21;

	protected boolean activateEndpoint;
	protected boolean collectValues;
	protected int maxMethodDepth;
	protected int maxSteps = 21;

	public TraceExtractorStepConfig(BreakpointConfig entrypoint, BreakpointConfig endpoint, VmConfig vm,
			LoggingConfig logging, int maxObjectDepth, boolean activateEndpoint, boolean collectValues,
			int maxMethodDepth, int maxSteps) {
		super(entrypoint, endpoint, vm, logging, maxObjectDepth);
		this.activateEndpoint = activateEndpoint;
		this.collectValues = collectValues;
		this.maxMethodDepth = maxMethodDepth;
		this.maxSteps = maxSteps;
	}

	public TraceExtractorStepConfig() {
		super();
	}

	public static TraceExtractorStepConfig fromJson(JsonNode rootNode) {
		TraceExtractorStepConfig config = new TraceExtractorStepConfig();
		config.fillFromJson(rootNode);
		return config;
	}

	public TraceExtractorStepConfigBuilder builder() {
		return new TraceExtractorStepConfigBuilder();
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
