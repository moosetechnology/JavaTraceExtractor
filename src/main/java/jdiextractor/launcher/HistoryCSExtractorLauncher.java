package jdiextractor.launcher;

import jdiextractor.config.CallStackHistoryExtractorConfig;
import jdiextractor.core.CallStackHistoryExtractor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Attach to a java virtual machine to extract the call stack to a text file
 * 
 * Extracts the call stack frame by frame during execution.
 * * <p><b>Note:</b> Extremely slow, but historically accurate.
 * Since data is serialized at every step, it preserves the exact state 
 * of objects as they were at the moment of execution.
 */
public class HistoryCSExtractorLauncher extends AbstractLauncher<CallStackHistoryExtractorConfig> {
	
	private final String configFileDefaultName = "configCSHistory.json";

	public static void main(String[] args) throws Exception {
		HistoryCSExtractorLauncher launcher = new HistoryCSExtractorLauncher();
		
		launcher.mainCore(args, new CallStackHistoryExtractor());
	}

	@Override
	protected CallStackHistoryExtractorConfig parseConfig(JsonNode node) {
		return CallStackHistoryExtractorConfig.fromJson(node);
	}

	@Override
	protected String configFileDefaultName() {
		return configFileDefaultName;
	}

}
