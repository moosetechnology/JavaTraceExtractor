# JavaTraceExtractor

JavaTraceExtractor is a dynamic analysis tool designed to extract Java execution traces and call stacks directly from a running Java Virtual Machine (JVM) into a text-based file.  
Provide ability to extract java traces compatible with [FamixJavaTrace](https://github.com/LeoDefossez/Famix-JavaTrace) enabling their analysis within the [Moose](https://moosetechnology.org/) software analysis platform by generating a dedicated meta-model.

It was first created to provide a parser for Java call stacks, **Support dropped since v1.0.0**.   
For More information about this meta-model see the [FamixCallStack](https://github.com/moosetechnology/FamixCallStack) project.

## ⚠ Prerequisites
**Critical Limit:** This tool **requires Java 9 or above** to run. However, it can attach to and analyze target applications running on *any* Java version.

## Installation

A detailed step-by-step guide is available: [Download & Import Tutorial](./utils/tutorials/download.md)

##  Usage Guide

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

NB. Click on Apply and on Close (do not click on Run in this step)

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
Run the target application followed by the selected launcher to begin extraction.

#### 3.4 Find the results
Results are generated at the root of the JavaTraceExtractor project. The name of the resulting file is given according to the user entries in the 'outputName : [..] , extension: [..]' field of your configXX.json file. For example, in the [`config.md`](utils/tutorials/config.md) file, the name of the output file will be JDIOutput.tr.

## How to use this generated trace? 

This generated trace can be imported in the [Famix-JavaTrace](https://github.com/moosetechnology/Famix-JavaTrace) project and can used to contextualize call graphs in the [Famix-CallGraphs-Contextualizer](https://github.com/moosetechnology/Famix-CallGraphs-Contextualizer) project.
