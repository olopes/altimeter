package org.psicover.altimeter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.psicover.altimeter.ui.AltimeterVisualization;

public class AltimeterUtil {
	
	public static void printUsage(Options options) {
		System.out.println("usage: AltimeterUtil [FILE]");
		System.out.println("       AltimeterUtil -c [-f <format>] [-n <session-number>] -o <OUTFILE> [FILE]");
		System.out.println();
		System.out.println("If no options are given, opens FILE in data visualization GUI");
		System.out.println("Otherwise performs file convertion");

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "AltimeterUtil", options, true );
	}

	public static void main(String[] args) {
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("h","help", false, "display this help message");
		options.addOption("c", "convert", false, "convert file");
		options.addOption("f", "format", true, "output format, use 'list' to show all available formats");
		options.addOption("n", "session-number", true, "session number to convert, defaults to 0");
		options.addOption("o", "output", true, "output file");
		
		DefaultParser parser = new DefaultParser();
		CommandLine cmd = null;		
		
	    try {
	        // parse the command line arguments
	    	cmd = parser.parse( options, args );
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	        
    		printUsage(options);
	        return;
	    }
	    
	    String [] otherArgs = cmd.getArgs();

	    String format = cmd.getOptionValue("format", "xlsx");
	    String outFile = cmd.getOptionValue("output");
	    String sessionNum = cmd.getOptionValue("session-number", "0");
	    
	    
	    
	    
	    // work with arguments
	    if(cmd.hasOption("format") && "list".equals(cmd.getOptionValue("format"))) {
	    	ExportData.printFormats();
	    }
	    
	    if(cmd.hasOption("convert")) {
	    	if(otherArgs.length!=1) {
	    		System.out.println("No input file given.");
	    		System.out.println();
	    		printUsage(options);
	    		return;
	    	}
	    	if(!cmd.hasOption("output")) {
	    		System.out.println("No output file given.");
	    		System.out.println();
	    		printUsage(options);
	    		return;
	    	}
			ExportData.export(format, otherArgs[0], sessionNum, outFile);
	    } else {
			AltimeterVisualization.main(otherArgs);
	    }
		
	}

}
