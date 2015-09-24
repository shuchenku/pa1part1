package cs131.pa1.filter.sequential;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import cs131.pa1.filter.sequential.SequentialREPL;

public class Cat extends SequentialFilter {

	private List<Scanner> scanners;
	private int scannerNumber;
	
	public Cat(List<String> fileNames) throws FileNotFoundException, InvalidArgumentException{
		
		if (fileNames.isEmpty()) {
			throw new InvalidArgumentException();
		}
		
		String cwd = SequentialREPL.currentWorkingDirectory;
		scanners = new ArrayList<Scanner>();
		scannerNumber = 0;
		for (String fileName : fileNames){
			Scanner fileScanner = new Scanner(new File(cwd + FILE_SEPARATOR + fileName));
			scanners.add(fileScanner);
		}
	}
	
	public Cat(String fileNames) throws FileNotFoundException, InvalidArgumentException{
		this(Arrays.asList(fileNames.split("\\s+")));
	}

	@Override
	public void process(){
		while (!isDone()){
			Scanner scan = scanners.get(scannerNumber);
			while (scan.hasNextLine()){
				output.add(scan.nextLine());
			}
			scannerNumber++;
		}
	}
	
	@Override
	protected String processLine(String line) {
		// This method should never be called, as we are overriding the
		// process method itself.
		return null;
	}

	@Override
	public boolean isDone() {
		return scanners.size() <= scannerNumber;
	}
}