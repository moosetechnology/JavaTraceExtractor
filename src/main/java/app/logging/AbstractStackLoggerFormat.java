package app.logging;

import java.io.FileWriter;
import java.io.IOException;

public abstract class AbstractStackLoggerFormat implements IStackLoggerFormat  {
	
	protected FileWriter output;

	public AbstractStackLoggerFormat(String outputName, String fileFormat) {

		try {
			output = new FileWriter(outputName + "." + fileFormat);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't open a file with the given name : " + outputName);
		}
	}

	@Override
	public void closeWriter() throws IOException {
		output.close();
	}
	
	protected void write(String str) {
		try {
			output.write(str);
		} catch(IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Couldn't write in the file");
		}
	}
	
	protected void writeln(String str) {
		try {
			output.write(str + "\n");
		} catch(IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Couldn't write in the file");
		}
	}

}
