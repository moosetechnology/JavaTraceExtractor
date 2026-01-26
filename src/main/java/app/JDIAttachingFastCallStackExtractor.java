package app;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.VirtualMachine;

import app.config.JDIExtractorConfig;
import app.extractor.CallstackFastExtractor;
import app.extractor.JDIExtractor;
import app.vmAttach.JDIAttach;

/**
 * Attach to a java virtual machine to extract the call stack to a text file
 * 
 * Extracts the call stack only once, when the breakpoint is reached.
 * * <p><b>Note:</b> Fast, but object states are captured at the very end. 
 * If an object was modified during execution, older frames will show the 
 * <i>current</i> modified value, not the value at the time of the call.
 */
public class JDIAttachingFastCallStackExtractor {

	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		// reading the config file
		String configFileName;
		JsonNode configNode = null;
		if (args.length == 0) {
			configFileName = "config.json";
		} else {
			configFileName = args[0];
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			configNode = mapper.readTree(new File(configFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}

		JDIExtractorConfig config = JDIExtractorConfig.fromJson(configNode);

		// creating the VmManager using JDIAttach to find the vm
		VirtualMachine vm = (new JDIAttach()).attachToJDI(config.getVm());

		JDIExtractor.launch(CallstackFastExtractor.class, vm, config);

		// Properly disconnecting
		vm.dispose();

		System.out.println("Execution took : " + (System.nanoTime() - startTime) + " nanoseconds");
	}

}
