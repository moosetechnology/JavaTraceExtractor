package jdiextractor.config;

import com.fasterxml.jackson.databind.JsonNode;

import jdiextractor.config.builder.CallStackSnapshotExtractorConfigBuilder;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.config.components.LoggingConfig;
import jdiextractor.config.components.VmConfig;

public class CallStackSnapshotExtractorConfig extends AbstractExtractorConfig {

	public CallStackSnapshotExtractorConfig(BreakpointConfig entrypoint, BreakpointConfig endpoint, VmConfig vm,
			LoggingConfig logging, int maxObjectDepth) {
		super(entrypoint, endpoint, vm, logging, maxObjectDepth);
	}

	public CallStackSnapshotExtractorConfig() {
		super();
	}

	public static CallStackSnapshotExtractorConfig fromJson(JsonNode rootNode) {
		CallStackSnapshotExtractorConfig config = new CallStackSnapshotExtractorConfig();
		config.fillFromJson(rootNode);
		return config;
	}

	public static CallStackSnapshotExtractorConfigBuilder builder() {
		return new CallStackSnapshotExtractorConfigBuilder();
	}

	@Override
	protected void fillFromJson(JsonNode rootNode) {
		super.fillFromJson(rootNode);

	}

}
