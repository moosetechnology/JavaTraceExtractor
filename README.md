# JavaCallStackExtractor 

JavaCallStackExtractor is a tool designed to extract as much information as possible from a Java call stack stored in a text-based file.  
It was created to provide a parser for Java call stacks, enabling their analysis within the [Moose](https://moosetechnology.org/) software analysis platform by generating a dedicated meta-model.   
For More information about this meta-model see the [FamixCallStack](https://github.com/LeoDefossez/FamixCallStack) project.


##  How to use

This tool requires Java version 9 to 21.  
Newer versions might work, but have not been tested.  
It is highly recommended to use the **same Java version** for both the program you want to analyze and the extractor.  
Using different versions may lead to unexpected behavior.  

### Step 0: Make at least a maven build to imports necessary libraries

### Step 1: Launch the program you want to analyze with the following VM argument:
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006 
``` 


In Eclipse, you can add this argument in the *Run Configuration > VM Arguments* section: 
	<img src="utils/image/run-Config-VMargs.png" width="460" height="331">  

This command enables debug mode on the Java VM:  
- jdwp: Java Debug Wire Protocol
- transport=dt_socket: Enables socket-based communication
- server=y: Starts the JVM in debug server mode (waits for a debugger)
- suspend=y: JVM execution is paused until a debugger connects
- address=5006: Port used to wait for debugger connection

### Step 2: Configure the Extractor
Edit the config.json file to fit the structure of your project.
Refer to  [config.md](utils/tutorials/config.md) for detailed instructions.

### Step 3: Run the Extractor
Run either :
- SnapshotCSExtractorLauncher : Fast, but object states are captured at the very end. 
	- Usefull for concepts
- HistoryCSExtractorLauncher : Slow, but historically accurate.
	- Usefull for in depth analysis
The output will be generated in the root directory of this repository.
