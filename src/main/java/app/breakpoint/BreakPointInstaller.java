package app.breakpoint;

import java.util.List;

import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import app.config.BreakpointConfig;

/**
 * This class offer methods to add a breakpoint on a method, from the method
 * description
 */
public class BreakPointInstaller {

	/**
	 * Add a breakpoint at a specified method on the given VM Precise the method
	 * argument type names in case there is multiple method having the same name
	 * 
	 * @param vm               the VM
	 * @param breakpointConfig information on the method at which the breakpoint is
	 *                         installed
	 */
	public static BreakpointWrapper addBreakpoint(VirtualMachine vm, BreakpointConfig breakpointConfig) {
		int repBefore = breakpointConfig.getRepBefore();
		// Getting the EventRequestManager of the VirtualMachine
		EventRequestManager requestManager = vm.eventRequestManager();

		// Getting the method, adapting the research depending of the amount of
		// information given
		Method method = findMethod(vm, breakpointConfig);

		// Getting the location of the method
		Location location = method.location();

		// Creating the breakpoint at the wanted location
		BreakpointRequest breakpointRequest = requestManager.createBreakpointRequest(location);

		// the +1 make it stop after "repBefore" number of encounter
		breakpointRequest.addCountFilter(1 + repBefore);
		breakpointRequest.enable(); // activate the breakpoint

		return new BreakpointWrapper(breakpointRequest, repBefore);
	}

	/**
	 * Find a method matching the given characteristics in the Virtual Machine
	 * 
	 * @param vm               the Virtual Machine
	 * @param breakpointConfig information on the method at which the breakpoint is
	 *                         installed
	 * @return the method if one match
	 */
	private static Method findMethod(VirtualMachine vm, BreakpointConfig breakpointConfig) {
		// finding the class
		List<ReferenceType> classes = vm.classesByName(breakpointConfig.getClassName());
		List<Method> allMethods = getMethods(breakpointConfig, classes);

		// if we have multiple methods and given arguments types, we search if one
		// correspond
		for (Method m : allMethods) {
			List<String> paramTypeNames = m.argumentTypeNames();
			// if not the same number of types just pass this method
			if (paramTypeNames.size() != breakpointConfig.getMethodArguments().size()) {
				continue;
			}
			// starting on the hypothesis that we found the method, and trying to invalidate
			// it
			boolean matches = true;
			for (int i = 0; i < paramTypeNames.size(); i++) {
				if (!paramTypeNames.get(i).equals(breakpointConfig.getMethodArguments().get(i))) {
					matches = false;
					break;
				}
			}
			// if we can't invalidate the method, then we found it
			if (matches) {
				return m;
			}
		}
		// if we got here, then no method have been found
		throw new IllegalArgumentException("No method named " + breakpointConfig.getMethodName() + " in class "
				+ breakpointConfig.getClassName() + " with argument types: " + breakpointConfig.getMethodArguments());
	}

	private static List<Method> getMethods(BreakpointConfig breakpointConfig, List<ReferenceType> classes) {
		if (classes.isEmpty()) {
			throw new IllegalArgumentException("Class not found : " + breakpointConfig.getClassName());
		}
		ClassType classType = (ClassType) classes.get(0);

		// getting all the methods with the searched name in the class
		List<Method> allMethods = classType.methodsByName(breakpointConfig.getMethodName());

		// if no method found throw an exception
		if (allMethods.isEmpty()) {
			throw new IllegalArgumentException("No method named " + breakpointConfig.getMethodName() + " in class "
					+ breakpointConfig.getClassName());
		}
		return allMethods;
	}

}