package info.devopsabyss;

/*

 * This is a nagios plugin to integrate Selenium Test Cases into Nagios.
 * Copyright (C) 2010 Christian Zunker (devops.abyss@googlemail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
//import com.util.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;


public class CallSeleniumTest {

	private final int NAGIOS_RC_OK = 0;
	private final int NAGIOS_RC_WARNING = 1;
	private final int NAGIOS_RC_CRITICAL = 2;
	private final int NAGIOS_RC_UNKNOWN = 3;

	private final String NAGIOS_TEXT_OK = "OK";
	private final String NAGIOS_TEXT_WARNING = "WARNING";
	private final String NAGIOS_TEXT_CRITICAL = "CRITICAL";
	private final String NAGIOS_TEXT_UNKNOWN = "UNKNOWN";

	private Options options = null;

	//TODO: compile java files only when no class file found. this way the user does not have to compile the sources.
	//		what is when i get compile errors? => return NAGIOS_UNKNOWN?

	private Result runJUnitTest(String className) throws ClassNotFoundException {
		Class<?> seleniumTestClass = Class.forName(className);
		return new JUnitCore().run(seleniumTestClass);
	}

	public static void main(String[] args) throws Exception {
		// TODO: Selenium Server host and port as parameters

		CallSeleniumTest seTest = new CallSeleniumTest();

		//Option(String opt, boolean hasArg, String description) 
		Option optionclass = new Option("c", "class", true, "full classname of testcase (required) e.g. \"com.example.tests.GoogleSeleniumTestCase\"");
		//optiontype.setRequired(true);
		Option optionverbose = new Option("v", "verbose", false, "show a lot of information (useful in case of problems)");
		//Option optionhost = new Option("H", "host", true, "hostname for selenium server");
		//Option optionport = new Option("p", "port", true, "port for selenium server");
		Option optionhelp = new Option("h", "help", false, "show this help screen");
		Option optionNagios3 = new Option("3", "nagios3", false, "in case of a test failure, print a multiline message in nagios 3 format");

		seTest.options = new Options();
		seTest.options.addOption(optionclass);
		seTest.options.addOption(optionverbose);
		//seTest.options.addOption(optionhost);
		//seTest.options.addOption(optionport);
		seTest.options.addOption(optionhelp);
		seTest.options.addOption(optionNagios3);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		// TODO: verify baseURL
		// TODO: is there a possibility to verify classname?

		String output = seTest.NAGIOS_TEXT_UNKNOWN + " - Error -  |";
		int nagios_rc = seTest.NAGIOS_RC_UNKNOWN;

		try {
			cmd = parser.parse(seTest.options, args);
			
			// has to be checked manually, otherwise you can't access the help message without specifying correct parameters
			if (cmd.hasOption("h") || cmd.getOptionValue("c") == null) {
				usage(seTest.options);
				System.exit(nagios_rc);
			}			
			
			if (cmd.hasOption("v")) {
				debug(cmd, seTest.options);
			}
			
			Result result = seTest.runJUnitTest(cmd.getOptionValue("c"));
			if (result.wasSuccessful()) {
				output = seTest.NAGIOS_TEXT_OK + " - " + cmd.getOptionValue("c") + " Tests passed | ExecTime=" + result.getRunTime() + "ms";
				nagios_rc = seTest.NAGIOS_RC_OK;
			} else {
				//String failureMessage = result.getFailures().toString();
				String failureMessage1 = result.getFailures().toString();
				String lines2[] = failureMessage1.split(System.getProperty("line.separator"));
				String failureMessage = lines2[0];
				
				output = seTest.NAGIOS_TEXT_CRITICAL + " - " + cmd.getOptionValue("c");
				if (cmd.hasOption("3")) {
					output += " Test Failures | ExecTime=" + result.getRunTime() + "ms\n"+ failureMessage;
				} else {
					output += " Test Failures: " + withoutNewlines(failureMessage) + " | ExecTime=" + result.getRunTime() + "ms";
				}
				nagios_rc = seTest.NAGIOS_RC_CRITICAL;
			}
		} catch (UnrecognizedOptionException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + "Parameter problems: " + messageWithoutNewlines(ex) + " |";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			usage(seTest.options);
		} catch (ParseException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + "Parameter problems: " + messageWithoutNewlines(ex) + " |";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			usage(seTest.options);
		} catch (NoClassDefFoundError ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": NoClassDefFoundError " + messageWithoutNewlines(ex) + " |";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			printStackTraceWhenVerbose(cmd, ex);
		} catch (ClassNotFoundException ex) {
			output = seTest.NAGIOS_TEXT_UNKNOWN + " - " + cmd.getOptionValue("c") + ": Testcase class " + messageWithoutNewlines(ex) + " not found! |";
			nagios_rc = seTest.NAGIOS_RC_UNKNOWN;
			printStackTraceWhenVerbose(cmd, ex);
		} catch (Exception ex) {
			output = seTest.NAGIOS_TEXT_CRITICAL + " - " + cmd.getOptionValue("c") + ": General Exception " + messageWithoutNewlines(ex) + " |";
			nagios_rc = seTest.NAGIOS_RC_CRITICAL;
			printStackTraceWhenVerbose(cmd, ex);
		} finally {
			System.out.println(output);
			System.exit(nagios_rc);
		}
	}

	private static String messageWithoutNewlines(final Throwable ex) {
		return withoutNewlines(ex.getMessage());
	}

	private static String withoutNewlines(final String message) {
		return message.replaceAll("\n", " ")
			.replaceAll("  ", " ")
			.replaceAll("  ", " ")
			.replaceAll("  ", " ");
	}

	private static void printStackTraceWhenVerbose(final CommandLine cmd, final Throwable ex) {
		if (cmd.hasOption("v")) {
			ex.printStackTrace();
		}
	}

	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("check_selenium", options);
		System.out.println("");
		System.out.println("This version of check_selenium was tested with:");
		System.out.println("  - selenium server 2.45.0");
		System.out.println("  - selenium ide 2.8.0");
		System.out.println("  - test case exported as JUnit 4 (Webdriver)");
		System.out.println("");
		System.out.println("Some example calls:");
		System.out.println(" ./check_selenium.sh -c \"com.example.tests.GoogleSeleniumWebdriverTestCase\"");
		System.out.println(" ./check_selenium.sh --class \"com.example.tests.GoogleSeleniumWebdriverTestCase\"");
	}
	
	private static void debug(final CommandLine cmd, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		System.out.println("----------------------------------------------------------");
		System.out.println("");
		formatter.printHelp("Verbose - check_selenium", options);
		System.out.println("");		
		System.out.println("----------------------------------------------------------");
		System.out.println("Nagios3 is : " + cmd.hasOption("3") );
		System.out.println("Verbose is : " + cmd.hasOption("v"));
		System.out.println("Class is   : " + cmd.getOptionValue("c"));
		//System.out.println("Host is    : " + cmd.getOptionValue("H"));
		//System.out.println("Port is    : " + cmd.getOptionValue("p"));
		System.out.println("");

	}
	
	
}
