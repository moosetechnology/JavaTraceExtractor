package app.csExtractors;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;

import app.breakpoint.BreakPointInstaller;
import app.breakpoint.BreakpointWrapper;
import app.config.JDIExtractorConfig;
import app.config.LoggingConfig;
import app.vmManager.VmManager;

public class CallstackExtractor {

	StackExtractor extractor;
	JDIExtractorConfig config;

	public CallstackExtractor(LoggingConfig loggingConfig, int maxDepth) {
		this.extractor = new StackExtractor(loggingConfig, maxDepth);
	}

	public CallstackExtractor(JDIExtractorConfig config) {
		this.extractor = new StackExtractor(config.getLogging(), config.getMaxDepth());
		this.config = config;
	}

	public static void extract(VirtualMachine vm, JDIExtractorConfig config) throws InterruptedException {
		VmManager vmManager = new VmManager(vm);

		// TODO This can help load classes that are not in jdk
		// ClassPrepareRequest classPrepareRequest =
		// vm.eventRequestManager().createClassPrepareRequest();
		// classPrepareRequest.addClassFilter(config.get("breakpoint").get("className").textValue());
		// classPrepareRequest.enable();

		// Adding the breakpoint
		BreakpointWrapper bkWrap = BreakPointInstaller.addBreakpoint(vm, config.getBreakpoint());

		// resuming the process of the thread
		vmManager.resumeThread(config.getEntryMethod());// TODO maybe it can be deleted

		vmManager.waitForBreakpoint(bkWrap);

		CallstackExtractor csExtractor = new CallstackExtractor(config.getLogging(), config.getMaxDepth());
		csExtractor.extractCallStack(vmManager.getThreadNamed(config.getEntryMethod()));

		// properly disconnecting
		vmManager.disposeVM();
	}

	/**
	 * Extract the call stack on the searched VM starting form the given thread and
	 * stopping at the method described
	 * 
	 * @param thread the thread to study
	 */
	public void extractCallStack(ThreadReference thread) {

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

}
