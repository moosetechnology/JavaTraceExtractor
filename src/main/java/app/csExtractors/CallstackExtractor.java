package app.csExtractors;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;

import app.breakpoint.BreakPointInstaller;
import app.breakpoint.BreakpointWrapper;
import app.config.BreakpointConfig;
import app.config.JDIExtractorConfig;

public class CallstackExtractor {

	StackExtractor extractor;
	VirtualMachine vm;
	JDIExtractorConfig config;

	public CallstackExtractor(VirtualMachine vm, JDIExtractorConfig config) {
		this.extractor = new StackExtractor(config.getLogging(), config.getMaxDepth());
		this.config = config;
		this.vm = vm;
	}

	/**
	 * Entry point of stack extraction, execute and extract the stack with the given
	 * configuration
	 * 
	 * @param vm     the vm on witch the extraction is done
	 * @param config the extraction configuration
	 */
	public static void extract(VirtualMachine vm, JDIExtractorConfig config) {
		CallstackExtractor csExtractor = new CallstackExtractor(vm, config);
		csExtractor.extractCallStack();
	}

	/**
	 * Extract the call stack following the configuration given at initialisation
	 */
	public void extractCallStack() {

		this.waitForBreakpoint();

		ThreadReference thread = this.getThreadNamed(config.getEntryMethod());

		try {
			extractor.getLogger().framesStart();
			// iterating from the end of the list to start the logging from the first method
			// called
			List<StackFrame> frames = thread.frames();
			ListIterator<StackFrame> it = frames.listIterator(frames.size());

			// doing the first iteration separately because the logging potentially need
			// to know if we are at the first element or not to join with a special
			// character
			extractor.getLogger().frameLineStart(1);

			// extracting the stack frame
			extractor.extract(it.previous());
			extractor.getLogger().frameLineEnd();

			for (int i = 2; i <= frames.size(); i++) {
				extractor.getLogger().joinElementListing();

				extractor.getLogger().frameLineStart(i);
				// extracting the stack frame
				extractor.extract(it.previous());
				extractor.getLogger().frameLineEnd();
			}
			extractor.getLogger().framesEnd();
		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}

		// close the writer in the logger
		extractor.closeLogger();

	}

	/**
	 * Returns the thread with the chosen name if one exist in the given VM
	 * 
	 * @param threadName name of the searched thread
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	private ThreadReference getThreadNamed(String threadName) {
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
	 * Waiting for the vm to stop at a break points
	 */
	private void waitForBreakpoint() {

		BreakpointConfig bkConfig = config.getBreakpoint();

		BreakpointWrapper bkWrap = null;
		// If class is already loaded, put the breakpoint directly
		if (BreakPointInstaller.isClassLoaded(vm, bkConfig.getClassName())) {
			bkWrap = BreakPointInstaller.addBreakpoint(vm, bkConfig);
		} else {
			// else create a ClassPrepareRequest to add the breakpoint later
			ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
			classPrepareRequest.addClassFilter(bkConfig.getClassName());
			classPrepareRequest.enable();
		}

		EventSet eventSet;
		boolean stop = false;
		try {
			while ((!stop && (eventSet = vm.eventQueue().remove()) != null)) {
				for (Event event : eventSet) {
					if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
						throw new IllegalStateException("VM disconnected or died before breakpoint");
					} else if (event instanceof BreakpointEvent) {
						if (bkWrap == null) {
							throw new IllegalStateException("Thread encountered a breakpoint while none has been set");
						}
						if (stop = bkWrap.shouldStopAt(event)) {
							// BreakPoint attained we can stop here
							break;
						}
					} else if (event instanceof ClassPrepareEvent) {
						if (BreakPointInstaller.isClassLoaded(vm, bkConfig.getClassName())) {
							// Adding the breakpoint
							bkWrap = BreakPointInstaller.addBreakpoint(vm, bkConfig);
						} else {
							throw new IllegalStateException(
									"ClassPrepareRequest catched on but the class is still not loaded");
						}
					} else {
						// TODO by looking at the type hierarchy of Event it could be possible to take
						// more steps into accounts
					}
				}

				// If not stopped, and no event left, resume the eventSet to be able to continue
				// on analysis
				if (!stop) {
					eventSet.resume();
				}

			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(
					"Cannot continue extraction due to an interruption of the vm connexion : " + e.getMessage());
		}
	}

}
