package dummies;

/**
 * This class offer a simple use of an enum value to check how enum are collected
 */
public class EnumValues {
	
	public static enum Animal {
		DOG;
	}

	public static void main(String[] args) {
		endpoint(Animal.DOG);
	}

	public static void endpoint(Animal d) {}
	

}
