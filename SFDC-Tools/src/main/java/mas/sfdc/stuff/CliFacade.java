package mas.sfdc.stuff;

import java.util.Map;

import mas.sfdc.orgTrack.ConnectUtil;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CliFacade {

	public static final String OPT_HELP = "help";
	public static final String OPT_USERNAME = "username";
	public static final String OPT_PASSWORD = "password";
	public static final String OPT_SERVERURL = "serverurl";
	public static final String OPT_APIVERSION = "apiversion";
	public static final String OPT_FILENAME = "filename";
	public static final String OPT_ENV = "env";

	public static final String ENV_USERNAME = "SFCR_USERNAME";
	public static final String ENV_PASSWORD = "SFCR_PASSWORD";
	public static final String ENV_SERVERURL = "SFCR_SERVERURL";
	public static final String ENV_FILENAME = "SFCR_FILENAME";
	public static final String ENV_APIVERSION = "SFCR_APIVERSION";

	// arguments read from commandline or environment
	private String username;
	private String password;
	private String filename = "package.xml";
	private String serverurl = ConnectUtil.DEFAULT_SERVERURL;
	private double apiVersion = ConnectUtil.DEFAULT_API_VERSION;

	public void getCommandLine(String[] arguments) {

		Options cliOptions = new Options();
		cliOptions.addOption(OPT_HELP, true, "generates this message");
		cliOptions.addOption(OPT_USERNAME, true, "userId");
		cliOptions.addOption(OPT_PASSWORD, true, "password");
		cliOptions.addOption(OPT_SERVERURL, true,
				"authentication url - optional");
		cliOptions
				.addOption(OPT_FILENAME, true, "filename to write - optional");
		cliOptions.addOption(OPT_APIVERSION, true, "API version - optional");
		cliOptions.addOption(OPT_ENV, false,
				"read all parameters from environment variables - optional");

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine commandLine = parser.parse(cliOptions, arguments, true);

			if (commandLine.hasOption(OPT_ENV)) {
				Map<String, String> envMap = System.getenv();
				this.username = envMap.get(ENV_USERNAME);
				this.password = envMap.get(ENV_PASSWORD);
				String tmpFileName = envMap.get(ENV_FILENAME);
				if (tmpFileName != null)
					this.filename = tmpFileName;
				String tmpServerurl = envMap.get(ENV_SERVERURL);
				if (tmpServerurl != null)
					this.serverurl = tmpServerurl;
				String tmpApiVersion = envMap.get(ENV_APIVERSION);
				if (tmpApiVersion != null)
					this.apiVersion = Double.parseDouble(tmpApiVersion);
			} else {
				this.username = commandLine.getOptionValue(OPT_USERNAME);
				this.password = commandLine.getOptionValue(OPT_PASSWORD);
				String tmpFileName = commandLine.getOptionValue(OPT_FILENAME);
				if (tmpFileName != null)
					this.filename = tmpFileName;
				String tmpServerurl = commandLine.getOptionValue(OPT_SERVERURL);
				if (tmpServerurl != null)
					this.serverurl = tmpServerurl;
				String tmpApiVersion = commandLine
						.getOptionValue(OPT_APIVERSION);
				if (tmpApiVersion != null)
					this.apiVersion = Double.parseDouble(tmpApiVersion);
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (this.username == null) {
			printErrorAndHelp("username not specified.", cliOptions);
			System.exit(1);
		} else if (this.password == null) {
			printErrorAndHelp("password not specified.", cliOptions);
			System.exit(1);
		}
	}

	private static void printErrorAndHelp(String message, Options cliOptions) {
		System.out.println(message);
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("SFCR", cliOptions);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFilename() {
		return filename;
	}

	public String getServerurl() {
		return serverurl;
	}

	public double getApiVersion() {
		return apiVersion;
	}

}
