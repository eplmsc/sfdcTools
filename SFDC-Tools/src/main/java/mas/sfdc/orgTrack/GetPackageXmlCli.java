package mas.sfdc.orgTrack;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import mas.sfdc.stuff.CliFacade;

import com.sforce.soap.metadata.FileProperties;

public class GetPackageXmlCli {
	public static void main(String[] args) {
		CliFacade cliFacade = new CliFacade();
		cliFacade.getCommandLine(args);

		ConnectUtil cu = new ConnectUtil(cliFacade.getServerurl(),
				cliFacade.getUsername(), cliFacade.getPassword(),
				cliFacade.getApiVersion());
		cu.connect();
		Map<String, FileProperties[]> metadataPropertiesMap = MetaDataGetter
				.getAllMetadataProperties(cu.getConnection());
		PrintStream filePrintStream = null;
		try {
			filePrintStream = new PrintStream(cliFacade.getFilename());
			ReportUtil.outputQualifiedPackageFormat(metadataPropertiesMap,
					filePrintStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (filePrintStream != null)
				filePrintStream.close();
		}
		System.out.println("Done.");
	}
}
