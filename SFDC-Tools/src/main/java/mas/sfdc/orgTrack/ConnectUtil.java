package mas.sfdc.orgTrack;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class ConnectUtil {

	public static final String SFDC_AUTHPATH = "/services/Soap/u/28.0";
	public static final double API_VERSION = 28.0;

	private final String userId;
	private final String token;
	private final String password;
	private final String authenticationUrl;

	private MetadataConnection connection;

	public ConnectUtil(String authenticationUrl, String userId, String token,
			String password) {
		super();
		this.authenticationUrl = authenticationUrl;
		this.userId = userId;
		this.token = token;
		this.password = password;
	}

	public void connect() {
		System.out.println("Logging in...");

		this.connection = null;

		String fullPassword = this.token != null ? this.password + this.token
				: this.password;

		ConnectorConfig partnerConfig = new ConnectorConfig();
		partnerConfig.setAuthEndpoint(this.authenticationUrl + SFDC_AUTHPATH);
		partnerConfig.setUsername(this.userId);
		partnerConfig.setPassword(fullPassword);

		PartnerConnection partnerConnection;
		try {
			partnerConnection = new PartnerConnection(partnerConfig);
			LoginResult loginResult = partnerConnection.login(this.userId,
					fullPassword);

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
		return userId;
	}

	public String getToken() {
		return token;
	}

	public String getPassword() {
		return password;
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}

	public MetadataConnection getConnection() {
		return connection;
	}
}
