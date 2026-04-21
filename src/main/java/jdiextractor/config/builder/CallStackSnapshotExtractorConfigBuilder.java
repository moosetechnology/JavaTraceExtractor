package jdiextractor.config.builder;

import jdiextractor.config.CallStackSnapshotExtractorConfig;

public class CallStackSnapshotExtractorConfigBuilder extends
		AbstractExtractorConfigBuilder<CallStackSnapshotExtractorConfigBuilder, CallStackSnapshotExtractorConfig> {

	@Override
	public CallStackSnapshotExtractorConfigBuilder self() {
		return this;
	}

	@Override
	public CallStackSnapshotExtractorConfig build() {
		ensureConfig();
		return new CallStackSnapshotExtractorConfig(entrypoint, endpoint, vmConfig, logging, maxObjectDepth);
	}

}
