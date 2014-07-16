package mas.sfdc.stuff;
import java.util.Map;

import mas.sfdc.orgTrack.MetaDataGetter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


public class GetOrg {


	public static final String OPT_USER = "user"; 
	public static final String OPT_PASSWORD = "password"; 
	public static final String OPT_TOKEN = "token"; 
	public static final String OPT_URL = "url"; 
	public static final String OPT_ZIP = "zip"; 
	public static final String OPT_API_VERSION = "api"; 
	
	public static final String OPT_HELP = "help"; 
	public static final String OPT_ENV = "env"; 
	
	public static final String ENV_USER = "SFCR_USER"; 
	public static final String ENV_PASSWORD = "SFCR_PASSWORD"; 
	public static final String ENV_TOKEN = "SFCR_TOKEN"; 
	public static final String ENV_URL = "SFCR_URL"; 
	public static final String ENV_ZIP = "SFCR_ZIP"; 
	public static final String ENV_API_VERSION = "SFCR_API"; 

	// arguments read from commandline or environment
	private String userId;
	private String password;
	private String token;
	private String authenticationUrl;
	private boolean zip;
	private String outputDirectory;
	private String apiVersion;

	public static final void main(String[] args) {
		GetOrg self = new GetOrg();
		self.getCommandLine(args);
		MetaDataGetter mdg = new MetaDataGetter();

	}
	
	public void getCommandLine(String[] arguments) {

		Options cliOptions = new Options();
		cliOptions.addOption(OPT_HELP, true, "generates this message");
		cliOptions.addOption(OPT_USER, true, "userId");
		cliOptions.addOption(OPT_PASSWORD, true, "password");
		cliOptions.addOption(OPT_TOKEN, true, "security token");
		cliOptions.addOption(OPT_URL, true, "authentication url");
		cliOptions.addOption(OPT_API_VERSION, true, "API Version");
		cliOptions.addOption(OPT_ZIP, false, "should be be saved as a zip-file");
		cliOptions.addOption(OPT_ENV, false, "read options from environment variables");
		
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine commandLine = parser.parse(cliOptions, arguments, true);
			if (commandLine.hasOption(OPT_ENV)) {
				Map<String, String> envMap = System.getenv();
				this.userId = envMap.get(ENV_USER);
				this.password = envMap.get(ENV_PASSWORD);
				this.token = envMap.get(ENV_TOKEN);
				this.authenticationUrl = envMap.get(ENV_URL);
				this.zip = envMap.containsKey(ENV_ZIP);
			} else {
				this.userId = commandLine.getOptionValue(OPT_USER);
				this.password = commandLine.getOptionValue(OPT_PASSWORD);
				this.token = commandLine.getOptionValue(OPT_TOKEN);
				this.authenticationUrl = commandLine.getOptionValue(OPT_URL);
				this.zip = commandLine.hasOption(OPT_ENV);
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (this.userId == null) {
			System.out.println("userId not specified.");
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp( "SFCR", cliOptions );
			System.exit(1);
		} else if (this.password == null) {
			System.out.println("password not specified.");
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp( "SFCR", cliOptions );
			System.exit(1);
		} else if (this.authenticationUrl == null) {
			System.out.println("url not specified.");
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp( "SFCR", cliOptions );
			System.exit(1);
		} else if (this.token == null) {
			System.out.println("token not specified.");
		} 
	}
}
