package cs131.pa1.filter.sequential;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cs131.pa1.filter.Message;

public class SequentialCommandBuilder {
	
	private final static Set<String> PIPES_IN = new HashSet<String>(Arrays.asList(new String[] {"Grep","Uniq","Wc","Fileprinter","OutPrinter"}));
	private final static Set<String> PIPES_OUT = new HashSet<String>(Arrays.asList(new String[] {"Cat","Ls","Pwd","Grep","Wc","Uniq"}));
	
	// Returns a list of SequentialFilters, one for each command and one for the output type
	public static List<SequentialFilter> createFiltersFromCommand(String command){	
		// Determining whether to print to console or file and removing redirection from command
		SequentialFilter finalFilter = determineFinalFilter(command);
		command = adjustCommandToRemoveFinalFilter(command);		
		List<String> subCommands = Arrays.asList(command.split("\\|"));
		List<SequentialFilter> filterPipeline = new ArrayList<SequentialFilter>();
		
		for (String subCommand : subCommands){
			SequentialFilter subFilter = constructFilterFromSubCommand(subCommand.trim());
			SequentialFilter prevFilter = !filterPipeline.isEmpty() ? filterPipeline.get(filterPipeline.size() - 1) : null;
			
			// isError is true if: command not found, invalid argument, file/directory not found
			boolean subCommandError = (subFilter instanceof OutPrinter) && ((OutPrinter)subFilter).isStandardError();
			if (subCommandError) {
				filterPipeline.clear();
				filterPipeline.add(subFilter);
				return filterPipeline;
			}
			// invalidPipe is true if the piping order is invalid
			SequentialFilter invalidPipe = ioInvalid(prevFilter, subFilter, subCommand);
			if (invalidPipe != null) {
				filterPipeline.clear();
				filterPipeline.add(invalidPipe);
				return filterPipeline;
			}
			filterPipeline.add(subFilter);
		}
		
		filterPipeline.add(finalFilter);
		linkFilters(filterPipeline);
		return filterPipeline;
	}

	// Returns an output filter with error message if there is a piping error
	private static SequentialFilter ioInvalid(SequentialFilter fromFilter, SequentialFilter toFilter, String subCommand) {
		boolean providesOutput = (fromFilter!=null) && PIPES_OUT.contains(fromFilter.getClass().getSimpleName());
		boolean requiresInput = PIPES_IN.contains(toFilter.getClass().getSimpleName());
		if (providesOutput && !requiresInput) {
			return new OutPrinter(Message.NO_INPUT.with_parameter(subCommand));
		} else if (!providesOutput && requiresInput) {
			return new OutPrinter(Message.REQUIRES_INPUT.with_parameter(subCommand));
		}
		return null;
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
