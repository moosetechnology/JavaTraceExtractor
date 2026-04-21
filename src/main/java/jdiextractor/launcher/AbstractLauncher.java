package jdiextractor.launcher;

import java.io.File;
import java.io.IOException;

import jdiextractor.config.AbstractExtractorConfig;
import jdiextractor.core.AbstractExtractor;
import jdiextractor.service.connector.JDIAttach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.VirtualMachine;

public abstract class AbstractLauncher<T extends AbstractExtractorConfig> {

	private long startTime;

	protected void mainCore(String[] args, AbstractExtractor<T> extractor) throws Exception {
		startRecordTime();

		
        JsonNode configNode = loadJsonNode(args);
        T config = parseConfig(configNode);

		// creating the VmManager using JDIAttach to find the vm
		VirtualMachine vm = (new JDIAttach()).attachToJDI(config.getVm());

		extractor.launch(vm, config);

		// Properly disconnecting
		try {
			vm.dispose();
		} catch (Exception e) {
			// An exception mean VM already died before
		}

		endRecordTime();
	}
	

	private void startRecordTime() {
		startTime = System.nanoTime();
	}

	private void endRecordTime() {
		System.out.println("Execution took : " + (System.nanoTime() - startTime) + " nanoseconds");
	}
	
	protected abstract T parseConfig(JsonNode node);
	
	protected abstract String configFileDefaultName();

    private JsonNode loadJsonNode(String[] args) {
        String fileName = (args.length == 0) ? this.configFileDefaultName() : args[0];
        try {
            return new ObjectMapper().readTree(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Config file not readable : ", e);
        }
    }

}
