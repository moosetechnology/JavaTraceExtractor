package dummies;

import java.util.ArrayList;
import java.util.List;

/**
 * Offer a chain of methods modifying an object in multiple steps Help check if
 * the model extracted can follow the evolution of the object or just take the last version of it
 */
public class ObjectEvolution {

	private static class Dog {
		private int age;

		public Dog(int age) {
			this.age = age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	}

	public static void main(String[] args) {
		Dog d = new Dog(1);
		changeAge(d);
	}

	public static void changeAge(Dog d) {
		d.setAge(2);
		endpoint(d);
	}

	public static void endpoint(Dog d) {
	}
}
