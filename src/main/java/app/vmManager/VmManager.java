package app.vmManager;

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

public class VmManager {

	VirtualMachine vm;

	public VmManager(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Returns the thread with the chosen name if one exist in the given VM
	 * 
	 * @param threadName name of the searched thread
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	public ThreadReference getThreadNamed(String threadName) {
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
	 * Waiting for the vm to stop at a break point
	 * 
	 * @param bkWrap the wrapper for the breakpoint searched
	 * @throws InterruptedException
	 */
	public void waitForBreakpoint(BreakpointWrapper bkWrap) throws InterruptedException {

		EventSet eventSet;
		boolean stop = false;

		while ((!stop && (eventSet = vm.eventQueue().remove()) != null)) {
			for (Event event : eventSet) {
				if (event instanceof VMDeathEvent) {
					throw new IllegalStateException("Thread has terminated without encountering the wanted breakpoint");
				} else if (event instanceof VMDisconnectEvent) {
					throw new IllegalStateException("VM has been disconnected");
				} else if (event instanceof BreakpointEvent && (stop = bkWrap.shouldStopAt(event))) {
					// BreakPoint attained we can stop here
					break;
				} else {
					// Not noticeable now
					// TODO by looking at the type hierarchy of Event it could be possible to take
					// steps into accounts
				}
			}
		}
	}

	/**
	 * Waiting for the vm to stop at a break point
	 * 
	 * @param bkWrap the wrapper for the breakpoint searched
	 * @throws InterruptedException
	 */
	public void waitForBreakpoint(VirtualMachine vm, BreakpointConfig config) throws InterruptedException {
		
		BreakpointWrapper bkWrap = null;
		// If class is already loaded, put the breakpoint directly
		if (BreakPointInstaller.isClassLoaded(vm, config.getClassName())) {
			bkWrap = BreakPointInstaller.addBreakpoint(vm, config);
		} else {
			// else create a ClassPrepareRequest to add the breakpoint later
			ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
			classPrepareRequest.addClassFilter(config.getClassName());
			classPrepareRequest.enable();
		}

		EventSet eventSet;
		boolean stop = false;

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
					if (BreakPointInstaller.isClassLoaded(vm, config.getClassName())) {
						// Adding the breakpoint
						bkWrap = BreakPointInstaller.addBreakpoint(vm, config);
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
	}

	/**
	 * Dispose of the VM to make sure the vm disconnect properly
	 */
	public void disposeVM() {
		vm.dispose();
	}

}
