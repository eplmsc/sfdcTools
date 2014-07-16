package mas.sfdc.orgTrack;

import java.util.Map;

import com.sforce.soap.metadata.FileProperties;

public class Tester {

	public static String userId = "martin.schroder@developer.org";
	public static String token = "7NScvttwawVcbNaYT3EGqfUre";
	public static String password = "vBay0001";
	public static String authenticationUrl = "https://login.salesforce.com";
	public static double apiVersion = 30.0;
	
	public static void main(String[] args) {

		testGetAllMetaDataProperties();
		
//		RetrieveResult rr = MetaDataGetter.retrieveAllOrgMetaData(authenticationUrl, userId, token, password);
//		MetaDataGetter.saveRetrieveResultAsZip(rr, "D:\\Data\\sletmig", "test.zip");
	}

	public static void testGetAllMetaDataProperties() {
		ConnectUtil cu = new ConnectUtil(authenticationUrl, userId, token, password);
		cu.connect();
		Map<String, FileProperties[]> metadataPropertiesMap = MetaDataGetter.getAllMetadataProperties(cu.getConnection());
		ReportUtil.outputQualifiedPackageFormat(metadataPropertiesMap, System.out);
	}
}
