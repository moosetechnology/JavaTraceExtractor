package jdiextractor.service.connector;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

import jdiextractor.config.components.VmConfig;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class JDIAttach {

	/**
	 * Attach to a java virtual machine located with the given information
	 * 
	 * @param config all information needed to find the vm
	 * @return the Virtual Machine if one found
	 * @throws IOException                        when unable to attach.
	 * @throws IllegalConnectorArgumentsException if no connector socket can be
	 *                                            used.
	 */
	public VirtualMachine attachToJDI(VmConfig config) throws IllegalConnectorArgumentsException, IOException {

		VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
		AttachingConnector connector = null;

		// Searching for the connector socket
		for (AttachingConnector ac : vmm.attachingConnectors()) {
			if (ac.name().equals("com.sun.jdi.SocketAttach")) {
				connector = ac;
				break;
			}
		}
		if (connector == null) {
			throw new IllegalStateException("No connector socket found");
		}

		// Configure the arguments
		Map<String, Connector.Argument> arguments = connector.defaultArguments();

		arguments.get("hostname").setValue(config.getHost());
		arguments.get("port").setValue(config.getPort()); // need to correspond to the JVM address

		// Parameters of the polling strategy
		int maxAttempts = 50; // 50 attemps * 100ms = 5 seconds of wating maximum
		long retryDelayMs = 100;

		// Connect to the JVM
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				return connector.attach(arguments);
			} catch (ConnectException e) {
				// If last attempt, then fail
				if (attempt == maxAttempts) {
					throw new IllegalStateException("Connection to JVM refused on port " + config.getPort() + " after "
							+ maxAttempts + " attempts. Target process might have crashed.", e);
				}
				// Else apply delay and retry
				try {
					Thread.sleep(retryDelayMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("JDI Attachment polling was interrupted", ie);
				}
			}
		}
		
		throw new IllegalStateException("Unreachable state in JDI polling loop.");
	}
}
