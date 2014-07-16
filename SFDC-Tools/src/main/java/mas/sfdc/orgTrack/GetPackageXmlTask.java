package mas.sfdc.orgTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sforce.soap.metadata.FileProperties;

public class GetPackageXmlTask extends Task {

	private String userId;
	private String password;
	private String token;
	private String authenticationUrl;
	private String targetDirName = ".";
	private String targetFileName = "package.xml";
	private double apiVersion = 30.0;

	@Override
	public void execute() throws BuildException {
		validateAttributes();
		
		String fullFileName = targetDirName + File.pathSeparator + targetFileName;
		
		ConnectUtil cu = new ConnectUtil(authenticationUrl, userId, token, password, apiVersion);
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
		if (userId == null) {
			throw new BuildException("You must set the userId attribute.");
		}
		if (password == null) {
			throw new BuildException("You must set the password attribute.");
		}
		if (authenticationUrl == null) {
			throw new BuildException("You must set the authenticationUrl attribute.");
		}		
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}
	public void setApiVersion(double apiVersion) {
		this.apiVersion = apiVersion;
	}


	public void setTargetDirName(String targetDirName) {
		this.targetDirName = targetDirName;
	}


	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}
}
