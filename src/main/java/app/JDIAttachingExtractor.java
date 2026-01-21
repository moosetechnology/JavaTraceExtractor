package app;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.VirtualMachine;

import app.config.JDIExtractorConfig;
import app.extractor.CallstackExtractor;
import app.extractor.JDIExtractor;
import app.vmAttach.JDIAttach;

/**
 * Attach to a java virtual machine to extract the call stack to a text file
 */
public class JDIAttachingExtractor {

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

		JDIExtractor.launch(CallstackExtractor.class, vm, config);

		// Properly disconnecting
		vm.dispose();

		System.out.println("Execution took : " + (System.nanoTime() - startTime) + " nanoseconds");
	}

}
