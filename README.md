# JavaCallStackExtractor

JavaCallStackExtractor is a tool designed to extract as much information as possible from a Java call stack stored in a text-based file.  
Provide ability to extract java traces compatible with [FamixJavaTrace](https://github.com/LeoDefossez/Famix-JavaTrace) enabling their analysis within the [Moose](https://moosetechnology.org/) software analysis platform by generating a dedicated meta-model.

It was first created to provide a parser for Java call stacks, **Support dropped since v1.0.0**.   
For More information about this meta-model see the [FamixCallStack](https://github.com/moosetechnology/FamixCallStack) project.

## Download the project
This section explain how to download the project, with eclipse as a reference.

**A step-by-step tutorial can be found here:** [How to download the project](./utils/tutorials/download.md)

##  How to use
> **This tool requires a Java version of 8 and below to work, but can analyze any Java version.**

### Step 1: Run a Maven build to import necessary libraries
There are two ways to do this:

1. Open a terminal at the root of the project and execute: `mvn clean install`
2. Right-click on the project in Eclipse:
   - Navigate to Maven > Update Project... 
   - Ensure your project is selected and click OK to force the update and resolve dependencies.

### Step 2: Launch the target program with the following VM arguments
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006 
```

In Eclipse, open the **Run Configurations** for your target application and paste this string into the **Arguments > VM Arguments section**:  
	<img src="utils/image/run-Config-VMargs.png" width="70%">  

This command enables debug mode on the Java VM:  
- jdwp: Java Debug Wire Protocol
- transport=dt_socket: Enables socket-based communication
- server=y: Starts the JVM in debug server mode (waits for a debugger)
- suspend=y: JVM execution is paused until a debugger connects
- address=5006: Port used to wait for debugger connection

### Step 3: Configure and Run the Extractor

#### 3.1 Choose Your Strategy
Select an extractor from the `jdiextractor.launcher` package based on your performance and accuracy needs:

| Extractor                     | Type            | Performance  | Output / Behavior                           | Default Config File     |
|:------------------------------|:----------------|:-------------|:--------------------------------------------|:------------------------|
| `SnapshotCSExtractorLauncher` | Call stack      | Fast         | Object states captured only at the very end | `configCSSnapshot.json` |
| `HistoryCSExtractorLauncher`  | Call stack      | Slow         | Historically accurate                       | `configCSHistory.json`  |
| `TraceExtractorStepLauncher`  | Execution trace | Slow & Heavy | Traces all method calls                     | `configTrace.json`      |

#### 3.2 Edit the Configuration
Edit your chosen `.json` file to match your project's structure. By default, the extractor automatically uses the configuration file linked to its algorithm.

> **Tip:** You can maintain multiple configurations by passing a custom file path as an argument to the extractor, preventing default overrides.

Refer to [`config.md`](utils/tutorials/config.md) for detailed configuration instructions.

#### 3.3 Execute
Run the selected launcher to begin extraction.
