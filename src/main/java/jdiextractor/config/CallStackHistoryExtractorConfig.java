package jdiextractor.config;

import com.fasterxml.jackson.databind.JsonNode;

import jdiextractor.config.builder.AbstractExtractorConfigBuilder;
import jdiextractor.config.builder.CallStackHistoryExtractorConfigBuilder;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.config.components.LoggingConfig;
import jdiextractor.config.components.VmConfig;

public class CallStackHistoryExtractorConfig extends AbstractExtractorConfig {

	public CallStackHistoryExtractorConfig(BreakpointConfig entrypoint, BreakpointConfig endpoint, VmConfig vm,
			LoggingConfig logging, int maxObjectDepth) {
		super(entrypoint, endpoint, vm, logging, maxObjectDepth);
	}

	public CallStackHistoryExtractorConfig() {
		super();
	}

	public static CallStackHistoryExtractorConfig fromJson(JsonNode rootNode) {
		CallStackHistoryExtractorConfig config = new CallStackHistoryExtractorConfig();
		config.fillFromJson(rootNode);
		return config;
	}
	
	public AbstractExtractorConfigBuilder<CallStackHistoryExtractorConfigBuilder, CallStackHistoryExtractorConfig> builder() {
		return new CallStackHistoryExtractorConfigBuilder();
	}

	@Override
	protected void fillFromJson(JsonNode rootNode) {
		super.fillFromJson(rootNode);
	}

}
