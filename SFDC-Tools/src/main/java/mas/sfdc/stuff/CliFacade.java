package mas.sfdc.stuff;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


public class CliFacade {

	public static final String OPT_HELP = "help"; 
	public static final String OPT_USER = "user"; 
	public static final String OPT_PASSWORD = "password"; 
	public static final String OPT_TOKEN = "token"; 
	public static final String OPT_URL = "url"; 
	public static final String OPT_AFTER = "after"; 
	public static final String OPT_BEFORE = "before"; 
	public static final String OPT_PACKAGE = "package"; 
	public static final String OPT_ENV = "env"; 
	
	public static final String ENV_USER = "SFCR_USER"; 
	public static final String ENV_PASSWORD = "SFCR_PASSWORD"; 
	public static final String ENV_TOKEN = "SFCR_TOKEN"; 
	public static final String ENV_URL = "SFCR_URL"; 
	public static final String ENV_AFTER = "SFCR_AFTER"; 
	public static final String ENV_BEFORE = "SFCR_BEFORE"; 

	// arguments read from commandline or environment
	private String userId;
	private String password;
	private String token;
	private String authenticationUrl;
	private boolean packageOutput;
	private Calendar beforeDate;
	private Calendar afterDate;

	public static final String DATE_FORMAT_OUT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_IN = "yyyy-MM-dd#HH:mm:ss";
	private SimpleDateFormat inputDateFormat = new SimpleDateFormat(DATE_FORMAT_IN);
	private SimpleDateFormat outputDateFormat = new SimpleDateFormat(DATE_FORMAT_OUT);
	
	public void getCommandLine(String[] arguments) {

		Options cliOptions = new Options();
		cliOptions.addOption(OPT_HELP, true, "generates this message");
		cliOptions.addOption(OPT_USER, true, "userId");
		cliOptions.addOption(OPT_PASSWORD, true, "password");
		cliOptions.addOption(OPT_TOKEN, true, "security token");
		cliOptions.addOption(OPT_URL, true, "authentication url");
		cliOptions.addOption(OPT_AFTER, true, "after datetime");
		cliOptions.addOption(OPT_BEFORE, true, "before datetime");
		cliOptions.addOption(OPT_PACKAGE, false, "produce package outpout");
		cliOptions.addOption(OPT_ENV, false, "read options from environment variables");
		
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine commandLine = parser.parse(cliOptions, arguments, true);
			this.packageOutput = commandLine.hasOption(OPT_PACKAGE);
			
			if (commandLine.hasOption(OPT_ENV)) {
				Map<String, String> envMap = System.getenv();
				this.userId = envMap.get(ENV_USER);
				this.password = envMap.get(ENV_PASSWORD);
				this.token = envMap.get(ENV_TOKEN);
				this.authenticationUrl = envMap.get(ENV_URL);

				String dateString = envMap.get(ENV_AFTER);
				if (dateString != null) {
					this.afterDate = Calendar.getInstance();
					this.afterDate.setTime(inputDateFormat.parse(dateString));
				}
				dateString = envMap.get(ENV_BEFORE);
				if (dateString != null) {
					this.beforeDate = Calendar.getInstance();
					this.beforeDate.setTime(inputDateFormat.parse(dateString));
				}
			} else {
				this.userId = commandLine.getOptionValue(OPT_USER);
				this.password = commandLine.getOptionValue(OPT_PASSWORD);
				this.token = commandLine.getOptionValue(OPT_TOKEN);
				this.authenticationUrl = commandLine.getOptionValue(OPT_URL);
			
				String dateString = commandLine.getOptionValue(OPT_AFTER);
				if (dateString != null) {
					this.afterDate = Calendar.getInstance();
					this.afterDate.setTime(inputDateFormat.parse(dateString));
				}
				dateString = commandLine.getOptionValue(OPT_BEFORE);
				if (dateString != null) {
					this.beforeDate = Calendar.getInstance();
					this.beforeDate.setTime(inputDateFormat.parse(dateString));
				}
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (java.text.ParseException e) {
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
		} else if (this.afterDate == null) {
			System.out.println("after date not specified.");
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp( "SFCR", cliOptions );
			System.exit(1);
		} else if (this.token == null) {
			System.out.println("token not specified.");
		} 
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}

	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}

	public boolean isPackageOutput() {
		return packageOutput;
	}

	public void setPackageOutput(boolean packageOutput) {
		this.packageOutput = packageOutput;
	}

	public Calendar getBeforeDate() {
		return beforeDate;
	}

	public void setBeforeDate(Calendar beforeDate) {
		this.beforeDate = beforeDate;
	}

	public Calendar getAfterDate() {
		return afterDate;
	}

	public void setAfterDate(Calendar afterDate) {
		this.afterDate = afterDate;
	}
}
