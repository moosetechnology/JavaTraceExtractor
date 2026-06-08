package dummies;

/**
 * Offer an implementation of a Inner class to verify the presence of type
 * container attribute of Inner classes
 */
public class ClassTypeContainerDoubleInnerClasses {

	public static class Dog {
		
		public static class Cat{
			public void bar() {
				ClassTypeContainerDoubleInnerClasses.endpoint(this);
			}
		}
		
		public void foo() {
			(new Cat()).bar();
		}
	}

	public static void main(String[] args) {
		Dog d = new Dog();

		d.foo();
	}

	public static void endpoint(Dog.Cat cat) {
	}

}
