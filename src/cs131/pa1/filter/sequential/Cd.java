package cs131.pa1.filter.sequential;

import java.io.File;
import java.io.IOException;

public class Cd extends SequentialFilter{
	boolean hasBeenSwapped;
	String swappingToLocation;
	
	public Cd (String directory) throws IOException, MissingArgumentException, TooManyArgumentsException {
		
		if (directory==null) {
			throw new MissingArgumentException();
		}
		
		if (directory.contains(" ")) {
			throw new TooManyArgumentsException();
		}
		
		hasBeenSwapped = false;
		String currentWorkingDirectory = SequentialREPL.currentWorkingDirectory;
		File newDir = new File(directory); 
		if (newDir.isAbsolute()){
			swappingToLocation = newDir.getCanonicalPath();
		} else {
			swappingToLocation = new File(currentWorkingDirectory+FILE_SEPARATOR+newDir.getPath()).getCanonicalPath();
		}	
		
		File f = new File (swappingToLocation);
		if (!f.exists() || !f.isDirectory()){
			throw new IOException();
		}
	}
	
	// Executes the CD command, which has not occurred before
	// this method is called.
	public void process(){
		SequentialREPL.currentWorkingDirectory = swappingToLocation;
		hasBeenSwapped = true;
	}
	
	@Override
	protected String processLine(String line) {
		// Should never be called, as we have overridden process.
		return null;
	}
}
