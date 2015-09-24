package cs131.pa1.filter.sequential;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cs131.pa1.filter.Message;

public class SequentialCommandBuilder {
	
	final static List<String> PIPES_IN = Arrays.asList(new String[] {"Grep","Uniq","Wc","Fileprinter","OutPrinter"});
	final static List<String> PIPES_OUT = Arrays.asList(new String[] {"Cat","Ls","Pwd","Grep","Wc","Uniq"});
	final static List<String> REJECTS_IN = Arrays.asList(new String[] {"Cat", "Ls", "Pwd", "Cd"});
	
	public static List<SequentialFilter> createFiltersFromCommand(String command){	

		SequentialFilter finalFilter = determineFinalFilter(command);
		command = adjustCommandToRemoveFinalFilter(command);		
		List<String> subCommands = Arrays.asList(command.split("\\|"));
		List<SequentialFilter> filterPipeline = new ArrayList<SequentialFilter>();
		
		for (String subCommand : subCommands){
			SequentialFilter subFilter = constructFilterFromSubCommand(subCommand.trim());
			SequentialFilter prevFilter = filterPipeline.size()>0? filterPipeline.get(filterPipeline.size()-1):null;
			
			boolean isError = (subFilter instanceof OutPrinter) && ((OutPrinter)subFilter).isStandardError();
			int validPipe = validateIO(prevFilter,subFilter);
			
			if (isError || validPipe > 0){
				
				if (validPipe == 1 && !isError) {
					subFilter = new OutPrinter(Message.REQUIRES_INPUT.with_parameter(subCommand));
				} else if (validPipe == 2 && !isError) {
					subFilter = new OutPrinter(Message.NO_INPUT.with_parameter(subCommand));
				}
				
				filterPipeline.clear();
				filterPipeline.add(subFilter);
				linkFilters(filterPipeline);
				return filterPipeline;
			}
			
			filterPipeline.add(subFilter);
		}
		
		filterPipeline.add(finalFilter);
		linkFilters(filterPipeline);
		return filterPipeline;
	}


	private static int validateIO(SequentialFilter fromFilter, SequentialFilter toFilter) {
		boolean gets_output = (fromFilter!=null) && PIPES_OUT.contains(fromFilter.getClass().getSimpleName());
		boolean requires_input = PIPES_IN.contains(toFilter.getClass().getSimpleName());
		boolean rejects_input = (fromFilter != null) && REJECTS_IN.contains(toFilter.getClass().getSimpleName());
		if (rejects_input) {
			return 2;
		} else if (gets_output != requires_input) {
			return 1;
		}
		return 0;
	}


	private static void linkFilters(List<SequentialFilter> filters){	
		for (int i = 0; i < filters.size() - 1; i++){
			filters.get(i).setNextFilter(filters.get(i+1));
		}
	}


	private static SequentialFilter constructFilterFromSubCommand(String subCommand){
		int startIdxOfParams = subCommand.contains(" ") ? subCommand.indexOf(' '):subCommand.length();
		String cmd = subCommand.substring(0,startIdxOfParams).toLowerCase();
		
		switch(cmd) {
		case "pwd":
			return new Pwd();
		case "ls":
			return new Ls();
		case "cd":
			try {
				return new Cd(subCommand.substring(startIdxOfParams).trim());
			} catch (IOException e) {
				return new OutPrinter(Message.DIRECTORY_NOT_FOUND.with_parameter(subCommand));
			}
		case "cat":
			try {
				return new Cat(subCommand.substring(startIdxOfParams).trim());
			} catch (FileNotFoundException | InvalidArgumentException e) {
				return new OutPrinter(Message.FILE_NOT_FOUND.with_parameter(subCommand));
			}
		case "grep":
			try {
				return new Grep(subCommand.substring(startIdxOfParams).trim());
			} catch (InvalidArgumentException e) {
				return new OutPrinter(Message.INVALID_ARGUMENT.with_parameter(cmd));
			}
		case "wc":
			return new Wc();
		case "uniq":
			return new Uniq();
		case ">":
			return new FilePrinter(subCommand);
		default:
			return new OutPrinter(Message.COMMAND_NOT_FOUND.with_parameter(subCommand));
		}	
	}
	
	private static String adjustCommandToRemoveFinalFilter(String command){
		if (command.contains(">")){
			return command.substring(0, command.lastIndexOf(">"));
		} else {
			return command;
		}
	}
	
	private static SequentialFilter determineFinalFilter(String command){
		if (command.contains(">")){
			String fileName = command.substring(command.lastIndexOf(">") + 1).trim();
			return new FilePrinter(fileName);
		}
		return new OutPrinter();
	}
	
	public static void startFilters(List<SequentialFilter> filters){
		for (SequentialFilter filter : filters){
			filter.process();
		}
	}
}
