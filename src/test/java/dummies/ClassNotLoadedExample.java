package dummies;

public class ClassNotLoadedExample {
	//TODO
	public static void main(String[] args) {
		// Never used 
		NeverUsed variableFantome = null; 
        
        // Si ton débogueur JDI place un breakpoint sur la ligne ci-dessous 
        // et demande le type de 'variableFantome', l'exception explosera.
        System.out.println("Breakpoint JDI ici."); 
    }

}

class NeverUsed { 
    int id;
}