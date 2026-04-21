package jdiextractor.service.breakpoint;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;

public class BreakpointWrapper {
	
	private BreakpointRequest breakpointRequest;
	private int repBefore;

	public BreakpointWrapper(BreakpointRequest breakpointRequest, int repBefore) {
		this.breakpointRequest = breakpointRequest;
		this.repBefore = repBefore;
	}

	/**
	 * Returns true iff this event should be resolved as a breakpoint
	 * @param event the event that could be resolved as a breakpoint
	 * @return true iff this event should be resolved as a breakpoint
	 */
	public boolean shouldStopAt(Event event) {
		boolean res = false;
		if(breakpointRequest.equals(event.request())) {
			res = repBefore == 0;
			repBefore--;
		}
		return res;
	}

}
