package jdiextractor.config.builder;

import jdiextractor.config.TraceExtractorStepConfig;

public class TraceExtractorStepConfigBuilder
		extends AbstractExtractorConfigBuilder<TraceExtractorStepConfigBuilder, TraceExtractorStepConfig> {

	protected boolean activateEndpoint = TraceExtractorStepConfig.DEFAULT_ACTIVATE_ENDPOINT;
	protected boolean collectValues = TraceExtractorStepConfig.DEFAULT_COLLECT_VALUES;
	protected int maxMethodDepth = TraceExtractorStepConfig.DEFAULT_MAX_METHOD_DEPTH;
	protected int maxSteps = TraceExtractorStepConfig.DEFAULT_MAX_STEPS;

	@Override
	public TraceExtractorStepConfigBuilder self() {
		return this;
	}

	public TraceExtractorStepConfigBuilder activateEndpoint(boolean activateEndpoint) {
		this.activateEndpoint = activateEndpoint;
		return self();
	}

	public TraceExtractorStepConfigBuilder collectValues(boolean collectValues) {
		this.collectValues = collectValues;
		return self();
	}

	public TraceExtractorStepConfigBuilder maxMethodDepth(int maxMethodDepth) {
		this.maxMethodDepth = maxMethodDepth;
		return self();
	}

	public TraceExtractorStepConfigBuilder maxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
		return self();
	}

	@Override
	public TraceExtractorStepConfig build() {
		ensureConfig();
		return new TraceExtractorStepConfig(entrypoint, endpoint, vmConfig, logging, maxObjectDepth, activateEndpoint,
				collectValues, maxMethodDepth, maxSteps);
	}

}
