package app.extractor;

import java.io.IOException;
import com.sun.jdi.IncompatibleThreadStateException;
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

/**
 * Extracts the call stack only once, when the breakpoint is reached.
 * * <p><b>Note:</b> Fast, but object states are captured at the very end. 
 * If an object was modified during execution, older frames will show the 
 * <i>current</i> modified value, not the value at the time of the call.
 */
public class CallstackFastExtractor extends AbstractCallStackExtractor {

	public CallstackFastExtractor(VirtualMachine vm, JDIExtractorConfig config) {
		super(vm, config, false);
	}

	@Override
	protected void executeExtraction() {
		try {
			this.waitForBreakpoint();
			this.processFrames(this.getThread());

		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Blocks execution until the configured breakpoint is hit. Handles
	 * ClassPrepareEvent if the class is not yet loaded.
	 */
	private void waitForBreakpoint() {

		BreakpointConfig bkConfig = config.getEndpoint();
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
