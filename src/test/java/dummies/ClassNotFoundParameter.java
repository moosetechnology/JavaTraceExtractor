package dummies;

/**
 * Provide a example of the use of ClassNotFoundException for a parameter of a method
 * Enable to check if JDI handle it as any other parameter
 */
public class ClassNotFoundParameter {
	
	public static void main(String[] args) {
		endpoint(null);
	}

	public static void endpoint(ClassNotFoundException ex) {
	}

}
