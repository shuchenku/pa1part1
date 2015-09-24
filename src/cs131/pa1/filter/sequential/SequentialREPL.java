package cs131.pa1.filter.sequential;

import java.util.List;
import java.util.Scanner;
import cs131.pa1.filter.Message;
import cs131.pa1.filter.sequential.SequentialCommandBuilder;
import cs131.pa1.filter.sequential.SequentialFilter;

public class SequentialREPL {

	static String currentWorkingDirectory;
	
	public static void main(String[] args){
		
		currentWorkingDirectory = System.getProperty("user.dir");
		System.out.print(Message.WELCOME);
		Scanner console = new Scanner(System.in);
		System.out.print(Message.NEWCOMMAND);
		while (console.hasNextLine()){
			String command = console.nextLine();
			if (command.equals("exit")){
				console.close();
				System.out.print(Message.GOODBYE);
				return;
			} else {
				List<SequentialFilter> filters = SequentialCommandBuilder.createFiltersFromCommand(command);
				SequentialCommandBuilder.startFilters(filters);
			}
			System.out.print(Message.NEWCOMMAND);
		}
		console.close();
	}
}
