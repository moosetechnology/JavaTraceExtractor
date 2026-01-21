package app.extractor;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import app.breakpoint.BreakPointInstaller;
import app.breakpoint.BreakpointWrapper;
import app.config.BreakpointConfig;
import app.config.JDIExtractorConfig;

public class CallstackExtractor extends JDIExtractor {

	private final StackFrameSerializer stackFrameSerializer;

	public CallstackExtractor(VirtualMachine vm, JDIExtractorConfig config) {
		super(vm, config);
		this.stackFrameSerializer = new StackFrameSerializer(config.getLogging(), config.getMaxDepth());
	}

	@Override
	protected void executeExtraction() { 
		try {
			// TODO this method does not work, it gives way too many frames (pay attention
			// this make the program way slower, except at least 5 minutes wait)
			// List<StackFrame> frames = this.collectFrames();
			// this.processFrames(frames);

			this.waitForBreakpoint();
			this.processFrames(this.getThread());

		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		} finally {
			// close the writer in the logger
			stackFrameSerializer.closeLogger();
		}
	}

	/**
	 * Iterates over the stack frames and delegates extraction to the
	 * StackExtractor.
	 */
	private void processFrames(ThreadReference thread) throws IncompatibleThreadStateException {
		processFrames(thread.frames());
	}

	/**
	 * Blocks execution until the configured breakpoint is hit. Handles
	 * ClassPrepareEvent if the class is not yet loaded.
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

	/**
	 * Iterates over the stack frames and delegates extraction to the
	 * StackExtractor.
	 */
	private void processFrames(List<StackFrame> frames) throws IncompatibleThreadStateException {
		stackFrameSerializer.getLogger().framesStart();
		// iterating from the end of the list to start the logging from the first method
		// called
		ListIterator<StackFrame> it = frames.listIterator(frames.size());

		// doing the first iteration separately because the logging potentially need
		// to know if we are at the first element or not to join with a special
		// character
		stackFrameSerializer.getLogger().frameLineStart(1);

		// extracting the stack frame
		stackFrameSerializer.extract(it.previous());
		stackFrameSerializer.getLogger().frameLineEnd();

		for (int i = 2; i <= frames.size(); i++) {
			stackFrameSerializer.getLogger().joinElementListing();

			stackFrameSerializer.getLogger().frameLineStart(i);
			// extracting the stack frame
			stackFrameSerializer.extract(it.previous());
			stackFrameSerializer.getLogger().frameLineEnd();
		}
		stackFrameSerializer.getLogger().framesEnd();

	}

	/**
	 * Blocks execution until the configured breakpoint is hit. Handles
	 * ClassPrepareEvent if the class is not yet loaded.
	 * 
	 * @throws IncompatibleThreadStateException
	 */
	private List<StackFrame> collectFrames() throws IncompatibleThreadStateException {
		Stack<StackFrame> frames = new Stack<>();
		ThreadReference targetThread = this.getThread();

		// Request method entries
		MethodEntryRequest entryReq = vm.eventRequestManager().createMethodEntryRequest();
		entryReq.addThreadFilter(targetThread);
		entryReq.enable();
		// Request method exits
		MethodExitRequest exitReq = vm.eventRequestManager().createMethodExitRequest();
		exitReq.addThreadFilter(targetThread);
		exitReq.enable();

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
					} else if (event instanceof MethodEntryEvent) {
						// If counts does not matches it means it is noise from the VM
						if (frames.size() + 1 == targetThread.frameCount()) {
							frames.push(targetThread.frame(0));
						}

					} else if (event instanceof MethodExitEvent) {
						frames.pop();
					}
				}

				// If not stopped, and no event left, resume the eventSet to be able to continue
				// on analysis
				if (!stop) {
					eventSet.resume();
				}

			}
		} catch (

		InterruptedException e) {
			throw new IllegalStateException(
					"Cannot continue extraction due to an interruption of the vm connexion : " + e.getMessage());
		}
		return frames;
	}

}
