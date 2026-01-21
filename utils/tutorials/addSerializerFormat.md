# Adding a new logging format 
JavaCallStackExtractor currently supports the following logging formats:
- json – A structured format suitable for integration with other tools.

## Want to know what data is available to the logger?
Check the interface [IStackSerializer](../../src/main/java/app/logging/IStackSerializer.java) to see which methods need to be implemented and what information the serializer has access to.

## Want to create your own logger ?
You can add a new serializer by implemeting IStackSerializer.
Here's a simplified example based on [LoggerJson](../../src/main/java/logging/LoggerJson.java) :
```java
public class StackSerializerJson implements IStackSerializer {

	public StackSerializerJson() {}
    
    // Implement required methods from ILoggerFormat here
}
```

To create your custom logger, simply provide concrete implementations for all methods defined in the ILoggerFormat interface.

## How to enable your logger in the application?
To make your custom logger available at runtime: 
1. Open [StackFrameLogger](../../src/main/java/app/extractor/StackFrameLogger.java)
2. Locate the method registerAllSerializer().
3. Add a new entry to the res map using the following format:
```declarative
    res.put("yourKeyword", YourLogger.class);
```
Replace yourKeyword with the identifier you want to use (e.g., "xml", "csv"), and YourLogger with the name of your custom logger class.