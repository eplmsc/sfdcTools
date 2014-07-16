package mas.sfdc.orgTrack;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class ConnectUtil {

	public static final String DEFAULT_SERVERURL = "https://login.salesforce.com";
	public static final double DEFAULT_API_VERSION = 30.0;

	private final String username;
	private final String password;
	private final String serverurl;
	private final double apiVersion;
	private final String sfdcAuthPath;

	private MetadataConnection connection;

	public ConnectUtil(String serverurl, String userId, String password) {
		this(serverurl, userId, password, DEFAULT_API_VERSION);
	}

	public ConnectUtil(String serverurl, String userId, String password,
			double apiVersion) {
		super();
		this.serverurl = serverurl;
		this.username = userId;
		this.password = password;
		this.apiVersion = apiVersion;

		this.sfdcAuthPath = "/services/Soap/u/" + String.valueOf(apiVersion);
	}

	public void connect() {
		System.out.println("Logging in...");

		this.connection = null;

		ConnectorConfig partnerConfig = new ConnectorConfig();
		partnerConfig.setAuthEndpoint(this.serverurl + this.sfdcAuthPath);
		partnerConfig.setUsername(this.username);
		partnerConfig.setPassword(password);

		PartnerConnection partnerConnection;
		try {
			partnerConnection = new PartnerConnection(partnerConfig);
			LoginResult loginResult = partnerConnection.login(this.username,
					this.password);

			ConnectorConfig metadataConfig = new ConnectorConfig();
			metadataConfig.setServiceEndpoint(loginResult
					.getMetadataServerUrl());
			metadataConfig.setSessionId(loginResult.getSessionId());

			this.connection = new MetadataConnection(metadataConfig);
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String getUserId() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getAuthenticationUrl() {
		return serverurl;
	}

	public MetadataConnection getConnection() {
		return connection;
	}

	public double getApiVersion() {
		return apiVersion;
	}

	public String getSfdcAuthPath() {
		return sfdcAuthPath;
	}
}
