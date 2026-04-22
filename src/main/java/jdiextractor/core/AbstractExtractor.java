package jdiextractor.core;

import java.util.List;
import java.util.Stack;

import jdiextractor.config.AbstractExtractorConfig;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.service.breakpoint.BreakPointInstaller;
import jdiextractor.service.breakpoint.BreakpointWrapper;
import jdiextractor.service.serializer.BufferedTraceConverter;
import jdiextractor.service.serializer.DefferedTraceConverter;
import jdiextractor.service.serializer.JDIToTraceConverter;
import jdiextractor.service.serializer.TraceSerializerJson;
import jdiextractor.tracemodel.entities.Trace;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.MethodExitRequest;

/**
 * Base framework for implementing JDI-based analysis tools.
 * <p>
 * This class abstracts the initialisation process of an extractor. Developers
 * should subclass this class to define custom extraction logic within
 * {@link #executeExtraction()}.
 * <p>
 * To run an extractor, use the static
 * {@link #launch(Class, VirtualMachine, AbstractExtractorConfig)} method, which
 * handles the instantiation and execution lifecycle.
 */
public abstract class AbstractExtractor<T extends AbstractExtractorConfig> {

	/**
	 * The target Virtual Machine being analyzed. Accessible to subclasses for
	 * adding requests and accessing memory.
	 */
	protected VirtualMachine vm;

	/**
	 * The configuration settings for the current extraction session.
	 */
	protected T config;

	/**
	 * The trace model built during execution
	 */
	protected JDIToTraceConverter jdiToTraceConverter;

	/**
	 * Whether the values are independents between all element of the trace or not
	 */
	protected boolean valuesIndependents;

	/**
	 * Does the extractor log or retain all element in a model It is useful for
	 * testing
	 */
	protected boolean activateLogging;

	protected Stack<Integer> branchIds;

	/**
	 * Only entry point to launch an extractor
	 * 
	 * @param vm     The target Virtual Machine.
	 * @param config The configuration object containing runtime settings.
	 */
	public void launch(VirtualMachine vm, T config) {
		this.setVM(vm);
		this.setConfig(config);
		this.createTracePopulator();
		this.executeExtraction();
	}

	/**
	 * Initialises the extractor
	 * 
	 * @param valuesIndependents whether the values are independents between all
	 *                           element of the trace or not
	 */
	protected AbstractExtractor(boolean valuesIndependents) {
		this.valuesIndependents = valuesIndependents;
		this.activateLogging = true;
		this.branchIds = new Stack<Integer>();
	}

	public AbstractExtractor(boolean valuesIndependents, boolean activateLogging) {
		this.valuesIndependents = valuesIndependents;
		this.activateLogging = activateLogging;
		this.branchIds = new Stack<Integer>();
	}

	public Trace getTrace() {
		return this.jdiToTraceConverter.getTrace();
	}

	/**
	 * Defines the main execution logic of the extractor.
	 * <p>
	 * Implement this method to define the specific behaviour of your tool. This
	 * method is automatically called by the framework after successful
	 * instantiation.
	 */
	protected abstract void executeExtraction();

	private void setConfig(T config) {
		this.config = config;
	}

	private void setVM(VirtualMachine vm) {
		this.vm = vm;
	}

	protected void createTracePopulator() {
		TraceSerializerJson serializer = new TraceSerializerJson(config.getLogging(), this.valuesIndependents);
		if (activateLogging) {
			this.jdiToTraceConverter = new BufferedTraceConverter(valuesIndependents, config.getObjectMaxDepth(),
					serializer);
		} else {
			this.jdiToTraceConverter = new DefferedTraceConverter(valuesIndependents, config.getObjectMaxDepth(),
					serializer);
		}
	}

	/**
	 * Returns the thread where the execution to extract takes place
	 * 
	 * @return the thread where the execution to extract takes place
	 */
	protected ThreadReference getThread() {
		// WARNING: We assume the thread name matches "main"
		return this.getThreadNamed("main");
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

	/**
	 * Process all events until the given breakpoint is encountered, if no
	 * breakpoint given process until end
	 * 
	 * @param bkConfig
	 * @throws IncompatibleThreadStateException
	 */
	protected void processEventsUntil(BreakpointConfig bkConfig) throws IncompatibleThreadStateException {

		BreakpointWrapper bkWrap = null;
		if (bkConfig != null) {
			if (BreakPointInstaller.isClassLoaded(vm, bkConfig.getClassName())) {
				bkWrap = BreakPointInstaller.addBreakpoint(vm, bkConfig);
			} else {
				ClassPrepareRequest cpReq = vm.eventRequestManager().createClassPrepareRequest();
				cpReq.addClassFilter(bkConfig.getClassName());
				cpReq.enable();
			}
		} else {
			// Adding a request to stop when the entry point end
			// We make the assumption that we only go though the entry point one time
			// TODO Maybe should also add an entryRequest to check if the method is called
			// again so that we don't stop too early
			MethodExitRequest exitReq = vm.eventRequestManager().createMethodExitRequest();
			exitReq.addClassFilter(config.getEntrypoint().getClassName());
			exitReq.enable();
		}

		vm.resume();
		EventSet eventSet;

		try {
			while ((eventSet = vm.eventQueue().remove()) != null) {
				for (Event event : eventSet) {

					if (event instanceof StepEvent) {
						this.reactToStepEvent((StepEvent) event);
					} else if (event instanceof MethodEntryEvent) {
						this.reactToMethodEntryEvent((MethodEntryEvent) event);
					} else if (event instanceof MethodExitEvent) {
						this.reactToMethodExitEvent((MethodExitEvent) event);
					}

					else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
						if (bkConfig != null) {
							// if bkConfig is not null then VM is not expected to terminate this way
							throw new IllegalStateException("VM disconnected or died before breakpoint");
						} else {
							// if bkConfig is null then VM is expected to terminate this way
							return;
						}
					}

					else if (event instanceof BreakpointEvent) {
						if (bkWrap == null) {
							throw new IllegalStateException("Thread encountered a breakpoint while none has been set");
						}
						if (bkWrap.shouldStopAt(event)) {
							return;
						}
					}

					else if (event instanceof ClassPrepareEvent) {
						if (BreakPointInstaller.isClassLoaded(vm, bkConfig.getClassName())) {
							bkWrap = BreakPointInstaller.addBreakpoint(vm, bkConfig);
						} else {
							throw new IllegalStateException("ClassPrepareRequest caught but class is still not loaded");
						}
					}
				}

				eventSet.resume();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interruption during extraction: " + e.getMessage());
		}
	}

	/**
	 * Process all events of the main thread until the VM dies or disconnect
	 * 
	 * @throws IncompatibleThreadStateException
	 */
	protected void processEventsUntilEnd() throws IncompatibleThreadStateException {
		try {
			this.processEventsUntil(null);
		} catch (com.sun.jdi.VMDisconnectedException e) {
			System.err.println("VM disconnected, tracing stopped");
		}
	}

	/**
	 * Create the TraceMethod associated to the frame, collecting values of receiver
	 * and arguments
	 * 
	 * @param frame the StackFrame of the TraceMethod
	 */
	protected void createMethodWith(StackFrame frame) {
		this.createMethodWith(frame, true);
	}

	/**
	 * Create the TraceMethod associated to the frame, collecting or not the values
	 * of receiver and arguments
	 * 
	 * @param frame          the StackFrame of the TraceMethod
	 * @param collectValues, true if value should be collected, false otherwise
	 */
	protected void createMethodWith(StackFrame frame, boolean collectValues) {
		Method method = frame.location().method();
		ObjectReference receiver = frame.thisObject();
		List<Value> argValues;
		int newMethodId = jdiToTraceConverter.newTraceElementId();

		try {
			argValues = frame.getArgumentValues();
		} catch (InternalException e) {
			// Happens for native calls, and can't be obtained
			argValues = null;
		}
		if (collectValues) {
			jdiToTraceConverter.newMethodFrom(method, argValues, receiver, newMethodId, this.getParentId());
		} else {
			jdiToTraceConverter.newMethodFrom(method, newMethodId, this.getParentId());
		}
		this.pushParentId(newMethodId);
	}

	protected void serializeTrace() {
		if (activateLogging) {
			this.jdiToTraceConverter.serialize();
		}
	}

	protected int popParentId() {
		return this.branchIds.pop();
	}

	protected void pushParentId(int id) {
		this.branchIds.push(id);
	}

	protected int getParentId() {
		if (this.branchIds.empty()) {
			// For the first method of the execution, we return a negative id, indicating it
			// has no parent
			return -1;
		}
		return this.branchIds.peek();
	}

	protected abstract void reactToMethodEntryEvent(MethodEntryEvent event);

	protected abstract void reactToMethodExitEvent(MethodExitEvent event);

	protected abstract void reactToStepEvent(StepEvent event);

}