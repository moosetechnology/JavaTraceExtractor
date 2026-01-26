## Default config.json example

```json
{
	"vm": {
		"host": "localhost",
		"port": "5006"
	},
	"entrypoint": {
		"className": "app.experiments.ObjectRefSimulation",
		"methodName": "main",
		"methodArguments": [
			"java.lang.String[]"
		],
		"repBefore": 0
	},
	"endpoint": {
		"className": "java.lang.Runtime",
		"methodName": "exec",
		"methodArguments": [
			"java.lang.String"
		],
		"repBefore": 0
	},
	"maxDepth" : 20,
	"logging": {
		"format"  : "json",
		"outputName" : "JDIOutput",
		"extension": "cs"
		
	}
}
}
```

## Using multiple configurations
By default, JavaCallstackExtractor looks for a file named config.json.
However, you can specify a custom configuration file by passing its name as a program argument to JavaCallstackExtractor (e.g., via Run Configurations in Eclipse).


## Configuration Explained

### vm
Defines the target Java Virtual Machine to attach to.
- host : The hostname of the target VM (typically "localhost" for local debugging)
- port : The port exposed by the target VM (should match the port in the JDWP setup, default is "5006")

### endpoint
Specifies where the call stack extraction should stop.
- className :  Fully qualified name of the class containing the target method
- methodName : Name of the method where the breakpoint will be set
- methodArguments : A list of parameter types (in order) as fully qualified class names
- repBefore : Number of times to skip the breakpoint before triggering

### entrypoint
Specifies the entry point in the same way as the endpoint

**Example :**  
Let’s say your main() method launches the epic journey of a brave hero. But—before launching it—you get jealous and decide that your hero should never get any rest...

<img src="/utils/image/callstack_example.png" alt="callstack example" width="50%">

In this case:
- The **entry method** is: main(String[] args), note that only the name of the method is actually important
- The **breakpoint** is: method rest() in the class Hero
```json
"sourceMethod": "main",
"breakpoint": {
  "className": "your.package.Hero",
  "methodName": "rest",
  "methodArguments": []
}
```

### maxDepth
Sets the maximum recursion depth for call stack logging.
Use a negative number for unlimited depth.

### logging
Controls how the output is formatted and where it is saved.
- format : Output format – currently supports "json"  
  (You can easily add new formats – see [addLoggerFormat.md](addLoggerFormat.md))
- outputName : Base name of the output file
- extension : File extension (default is "cs").  
  This is important for compatibility with import tools like [FamixCallStack](https://github.com/LeoDefossez/FamixCallStack#)
