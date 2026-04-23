package dummies;

/**
 * This class offer a simple Anonymous class of an Object 
 * It is basically the same as AnonymousClass, but we use Object here to check how it is handled
 */
public class AnonymousObjectClass {


	public static void main(String[] args) {
		Object o = new Object() {
			public String toString() {
				endpoint(this);
				return "";
			}
		};

		o.toString();
	}

	public static void endpoint(Object o) {
	}

}
