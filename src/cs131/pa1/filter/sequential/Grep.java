package cs131.pa1.filter.sequential;

public class Grep extends SequentialFilter {
	
	private String searchPattern;
	
	public Grep(String pattern) throws InvalidArgumentException{
		
		if (pattern==null) {
			throw new InvalidArgumentException();
		}
		this.searchPattern = pattern;
	}
	
	@Override
	protected String processLine(String line) {
		if (line.contains(searchPattern)){
			return line;
		}
		return null;
	}
}
