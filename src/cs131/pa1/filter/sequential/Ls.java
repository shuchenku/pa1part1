package cs131.pa1.filter.sequential;

import java.io.File;

public class Ls extends SequentialFilter {
	
	@Override
	public void process(){
		String currentWorkingDirectory = SequentialREPL.currentWorkingDirectory;
		File directoryObject = new File(currentWorkingDirectory);
		String[] files = directoryObject.list();
		for (String file : files){
			this.output.add(file);
		}
	}
	
	@Override
	protected String processLine(String line) {
		// Should never be called, as we have overridden process.
		return null;
	}
}
