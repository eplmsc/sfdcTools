package mas.sfdc.orgTrack;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sforce.soap.metadata.FileProperties;

public class GetPackageXmlTask extends Task {

	private String username;
	private String password;
	private String serverurl = ConnectUtil.DEFAULT_SERVERURL;
	private double apiVersion = ConnectUtil.DEFAULT_API_VERSION;
	private String targetFileName = "package.xml";

	@Override
	public void execute() throws BuildException {
		validateAttributes();
		
		ConnectUtil cu = new ConnectUtil(serverurl, username, password, apiVersion);
		cu.connect();
		Map<String, FileProperties[]> metadataPropertiesMap = MetaDataGetter.getAllMetadataProperties(cu.getConnection());
		PrintStream filePrintStream = null;
		try {
			filePrintStream = new PrintStream(targetFileName);
			ReportUtil.outputQualifiedPackageFormat(metadataPropertiesMap, filePrintStream);
		} catch (FileNotFoundException e) {
			throw new BuildException(e.getMessage());
		} finally {
			if (filePrintStream != null) filePrintStream.close();
		}
	}

	
	private void validateAttributes() {
		if (username == null) {
			throw new BuildException("You must set the userId attribute.");
		}
		if (password == null) {
			throw new BuildException("You must set the password attribute.");
		}
	}


	public void setUserId(String userId) {
		this.username = userId;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setAuthenticationUrl(String authenticationUrl) {
		this.serverurl = authenticationUrl;
	}
	public void setApiVersion(double apiVersion) {
		this.apiVersion = apiVersion;
	}
	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}
}
