package jdiextractor.launcher;

import jdiextractor.config.TraceExtractorStepConfig;
import jdiextractor.core.TraceExtractorStep;

import com.fasterxml.jackson.databind.JsonNode;

public class TraceExtractorStepLauncher extends AbstractLauncher<TraceExtractorStepConfig> {
	
private final String configFileDefaultName = "configTrace.json";
	
	public static void main(String[] args) throws Exception {
		TraceExtractorStepLauncher launcher = new TraceExtractorStepLauncher();

		launcher.mainCore(args, new TraceExtractorStep());
	}

	@Override
	protected TraceExtractorStepConfig parseConfig(JsonNode node) {
		return TraceExtractorStepConfig.fromJson(node);
	}

	@Override
	protected String configFileDefaultName() {
		return configFileDefaultName;
	}

}
