package jdiextractor.config.builder;

import jdiextractor.config.CallStackHistoryExtractorConfig;

public class CallStackHistoryExtractorConfigBuilder extends
		AbstractExtractorConfigBuilder<CallStackHistoryExtractorConfigBuilder, CallStackHistoryExtractorConfig> {

	@Override
	public CallStackHistoryExtractorConfigBuilder self() {
		return this;
	}

	@Override
	public CallStackHistoryExtractorConfig build() {
		ensureConfig();
		return new CallStackHistoryExtractorConfig(entrypoint, endpoint, vmConfig, logging, maxObjectDepth);
	}

}
