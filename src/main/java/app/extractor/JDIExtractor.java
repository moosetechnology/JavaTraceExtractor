package app.extractor;

import java.lang.reflect.Constructor;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import app.config.JDIExtractorConfig;

/**
 * Base framework for implementing JDI-based analysis tools.
 * <p>
 * This class abstracts the initialisation process of an extractor. Developers
 * should subclass this class to define custom extraction logic within
 * {@link #executeExtraction()}.
 * <p>
 * To run an extractor, use the static
 * {@link #launch(Class, VirtualMachine, JDIExtractorConfig)} method, which
 * handles the instantiation and execution lifecycle.
 */
public abstract class JDIExtractor {

	/**
	 * The target Virtual Machine being analyzed. Accessible to subclasses for
	 * adding requests and accessing memory.
	 */
	protected final VirtualMachine vm;

	/**
	 * The configuration settings for the current extraction session.
	 */
	protected final JDIExtractorConfig config;

	/**
	 * Initializes the extractor context.
	 * <p>
	 * <b>Note to implementors:</b> Your subclass MUST expose a public constructor
	 * matching this signature to be compatible with the
	 * {@link #launch(Class, VirtualMachine, JDIExtractorConfig)} facility.
	 *
	 * @param vm     The target Virtual Machine.
	 * @param config The configuration object containing runtime settings.
	 */
	public JDIExtractor(VirtualMachine vm, JDIExtractorConfig config) {
		this.vm = vm;
		this.config = config;
	}

	/**
	 * Defines the main execution logic of the extractor.
	 * <p>
	 * Implement this method to define the specific behavior of your tool. This
	 * method is automatically called by the framework after successful
	 * instantiation.
	 */
	protected abstract void executeExtraction();

	/**
	 * Bootstraps and executes a specific extractor strategy.
	 * <p>
	 * This utility method handles the instantiation of the provided
	 * {@code extractorClass} using reflection and triggers the
	 * {@link #executeExtraction()} method.
	 *
	 * @param extractorClass The concrete class of the extractor to run (e.g.,
	 *                       {@code CallstackExtractor.class}).
	 * @param vm             The connected Virtual Machine instance.
	 * @param config         The configuration to use for this session.
	 * @throws IllegalArgumentException If the provided class does not have a public
	 *                                  constructor accepting
	 *                                  {@code (VirtualMachine, JDIExtractorConfig)}.
	 * @throws RuntimeException         If the extractor fails to initialize or
	 *                                  encounters a critical error during
	 *                                  execution.
	 */
	public static void launch(Class<? extends JDIExtractor> extractorClass, VirtualMachine vm,
			JDIExtractorConfig config) {
		Constructor<? extends JDIExtractor> constructor;
		JDIExtractor instance;

		// 1. Locate the required constructor
		try {
			constructor = extractorClass.getConstructor(VirtualMachine.class, JDIExtractorConfig.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Invalid Extractor implementation: The class " + extractorClass.getName()
					+ " must provide a public constructor accepting (VirtualMachine, JDIExtractorConfig).");
		}
		// 2. Instantiate the concrete extractor
		try {
			instance = constructor.newInstance(vm, config);
		} catch (Exception e) {
			throw new RuntimeException("Critical failure while launching the extractor: " + e.getMessage(), e);
		}
		// 3. Trigger the extraction logic
		instance.executeExtraction();

	}

	/**
	 * Returns the thread where the execution to extract takes place
	 * 
	 * @return the thread where the execution to extract takes place
	 */
	protected ThreadReference getThread() {
		// WARNING: We assume the thread name matches 'entryMethod' (usually "main")
		return this.getThreadNamed(config.getEntrypoint().getMethodName());
	}

	/**
	 * Returns the thread with the chosen name if one exist in the given VM
	 * 
	 * @param threadName name of the searched thread
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	protected ThreadReference getThreadNamed(String threadName) {
		ThreadReference main = null;
		for (ThreadReference thread : vm.allThreads()) {
			if (thread.name().equals(threadName)) {
				main = thread;
				break;
			}
		}
		if (main == null) {
			throw new IllegalStateException("No thread named " + threadName + "was found");
		}
		return main;
	}
}