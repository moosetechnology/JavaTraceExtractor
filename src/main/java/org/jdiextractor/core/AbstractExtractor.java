package org.jdiextractor.core;

import java.util.List;

import org.jdiextractor.config.AbstractExtractorConfig;
import org.jdiextractor.config.components.BreakpointConfig;
import org.jdiextractor.service.breakpoint.BreakPointInstaller;
import org.jdiextractor.service.breakpoint.BreakpointWrapper;
import org.jdiextractor.service.serializer.JDIToTraceConverter;

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
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;

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

	protected abstract void createTracePopulator();

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
		}

		vm.resume();
		EventSet eventSet;

		try {
			while ((eventSet = vm.eventQueue().remove()) != null) {
				for (Event event : eventSet) {

					if (event instanceof StepEvent) {
						this.reactToStepEvent((StepEvent) event);
					}
					if (event instanceof MethodEntryEvent) {
						this.reactToMethodEntryEvent((MethodEntryEvent) event);
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
		this.processEventsUntil(null);
	}

	protected void createMethodWith(StackFrame frame) {
		Method method = frame.location().method();
		ObjectReference receiver = frame.thisObject();
		List<Value> argValues;

		try {
			argValues = frame.getArgumentValues();
		} catch (InternalException e) {
			// Happens for native calls, and can't be obtained
			argValues = null;
		}

		jdiToTraceConverter.newMethodFrom(method, argValues, receiver);
	}

	protected void serializeTrace() {
		this.jdiToTraceConverter.serialize();
	}

	protected abstract void reactToMethodEntryEvent(MethodEntryEvent event);

	protected abstract void reactToStepEvent(StepEvent event);

}