package org.jdiextractor.launcher;

import org.jdiextractor.config.CallStackSnapshotExtractorConfig;
import org.jdiextractor.core.CallStackSnapshotExtractor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Attach to a java virtual machine to extract the call stack to a text file
 * 
 * Extracts the call stack only once, when the breakpoint is reached. *
 * <p>
 * <b>Note:</b> Fast, but object states are captured at the very end. If an
 * object was modified during execution, older frames will show the
 * <i>current</i> modified value, not the value at the time of the call.
 */
public class SnapshotCSExtractorLauncher extends AbstractLauncher<CallStackSnapshotExtractorConfig> {

	private final String configFileDefaultName = "configCSSnapshot.json";
	
	public static void main(String[] args) throws Exception {
		SnapshotCSExtractorLauncher launcher = new SnapshotCSExtractorLauncher();

		launcher.mainCore(args, new CallStackSnapshotExtractor());
	}

	@Override
	protected CallStackSnapshotExtractorConfig parseConfig(JsonNode node) {
		return CallStackSnapshotExtractorConfig.fromJson(node);
	}

	@Override
	protected String configFileDefaultName() {
		return configFileDefaultName;
	}
}
