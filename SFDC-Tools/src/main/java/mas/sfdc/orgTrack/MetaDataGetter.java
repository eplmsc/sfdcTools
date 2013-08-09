package mas.sfdc.orgTrack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

import com.sforce.soap.metadata.APIAccessLevel;
import com.sforce.soap.metadata.AsyncRequestState;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Package;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.RetrieveMessage;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.ws.ConnectionException;

public class MetaDataGetter {

	public static final long ONE_SECOND = 1000;
	public static final int MAX_POLL_REQUEST = 7;

	public static void main(String[] args) {
		CliFacade cliFacade = new CliFacade();
		cliFacade.getCommandLine(args);

		ConnectUtil connectUtil = new ConnectUtil(
				cliFacade.getAuthenticationUrl(), cliFacade.getUserId(),
				cliFacade.getToken(), cliFacade.getPassword());
		connectUtil.connect();

		// ReportUtil.outputQualifiedPackageFormat(getAllMetadataProperties(connectUtil.getConnection()));
		//
		// System.out.println();System.out.println();
		//
		// ReportUtil.outputNonSpecificPackageFormat(describeMetadata(connectUtil.getConnection()));

		uglyMethodInNeedOfRefactoring(connectUtil.getConnection());
	}

	static public DescribeMetadataResult describeMetadata(
			MetadataConnection metadataConnection) {
		System.out.println("Getting metadata description...");
		DescribeMetadataResult dmr = null;
		try {
			dmr = metadataConnection.describeMetadata(ConnectUtil.API_VERSION);
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return dmr;
	}

	static public void uglyMethodInNeedOfRefactoring(
			MetadataConnection metadataConnection) {
		System.out.println("Getting metadata zip-file...");
		DescribeMetadataResult dmr = null;
		try {
			dmr = metadataConnection.describeMetadata(ConnectUtil.API_VERSION);

			final String[] asterisk = new String[] { "*" };
			PackageTypeMembers[] ptmArray = new PackageTypeMembers[dmr
					.getMetadataObjects().length];
			Package unpackaged = new Package();
			unpackaged.setVersion(Double.toString(ConnectUtil.API_VERSION));
			unpackaged.setApiAccessLevel(APIAccessLevel.Unrestricted);
			unpackaged.setTypes(ptmArray);

			for (int i = 0; i < dmr.getMetadataObjects().length; i++) {
				if (!"InstalledPackage".equalsIgnoreCase(dmr.getMetadataObjects()[i].getXmlName())) {
					ptmArray[i] = new PackageTypeMembers();
					ptmArray[i].setMembers(asterisk);
					ptmArray[i].setName(dmr.getMetadataObjects()[i].getXmlName());
				}
			}

			RetrieveRequest request = new RetrieveRequest();
			request.setUnpackaged(unpackaged);
			request.setApiVersion(ConnectUtil.API_VERSION);

			System.out.println("Issuing retrieve request...");
			AsyncResult asyncResult = metadataConnection.retrieve(request);

			// Wait for the retrieve to complete
			int poll = 0;
			long waitTimeMilliSecs = ONE_SECOND;
			while (!asyncResult.isDone()) {
				System.out.println("Polling result again in "+waitTimeMilliSecs/1000 +" seconds");
				Thread.sleep(waitTimeMilliSecs);
				// double the wait time for the next iteration
				waitTimeMilliSecs *= 2;
				if (poll++ > MAX_POLL_REQUEST) {
					throw new Exception(
							"Request timed out.  If this is a large set "
									+ "of metadata components, check that the time allowed "
									+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
				}
				asyncResult = metadataConnection
						.checkStatus(new String[] { asyncResult.getId() })[0];
				System.out.println("Status is: " + asyncResult.getState());
			}

			if (asyncResult.getState() != AsyncRequestState.Completed) {
				throw new Exception(asyncResult.getStatusCode() + " msg: "
						+ asyncResult.getMessage());
			}

			RetrieveResult result = metadataConnection
					.checkRetrieveStatus(asyncResult.getId());

			// Print out any warning messages
			StringBuilder buf = new StringBuilder();
			if (result.getMessages() != null) {
				for (RetrieveMessage rm : result.getMessages()) {
					buf.append(rm.getFileName() + " - " + rm.getProblem());
				}
			}
			if (buf.length() > 0) {
				System.out.println("Retrieve warnings:\n" + buf);
			}

			// Write the zip to the file system
			System.out.println("Writing results to zip file");
			ByteArrayInputStream bais = new ByteArrayInputStream(
					result.getZipFile());
			File resultsFile = new File("SFDCTools.retrieveResults.zip");
			FileOutputStream os = new FileOutputStream(resultsFile);
			try {
				ReadableByteChannel src = Channels.newChannel(bais);
				FileChannel dest = os.getChannel();
				copy(src, dest);

				System.out.println("Results written to "
						+ resultsFile.getAbsolutePath());
			} finally {
				os.close();
			}

		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return;
	}

	/**
	 * Helper method to copy from a readable channel to a writable channel,
	 * using an in-memory buffer.
	 */
	private static void copy(ReadableByteChannel src, WritableByteChannel dest)
			throws IOException {
		// use an in-memory byte buffer
		ByteBuffer buffer = ByteBuffer.allocate(8092);
		while (src.read(buffer) != -1) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				dest.write(buffer);
			}
			buffer.clear();
		}
	}

	static public Map<String, FileProperties[]> getAllMetadataProperties(
			MetadataConnection metadataConnection) {
		System.out.println("Getting metadata...");
		Map<String, FileProperties[]> result = new HashMap<String, FileProperties[]>();
		try {
			DescribeMetadataResult dmr = metadataConnection
					.describeMetadata(ConnectUtil.API_VERSION);
			ListMetadataQuery[] queries = new ListMetadataQuery[1];
			for (int i = 0; i < dmr.getMetadataObjects().length; i++) {
				queries[0] = new ListMetadataQuery();
				queries[0].setType(dmr.getMetadataObjects()[i].getXmlName());

				FileProperties[] queryResult = metadataConnection.listMetadata(
						queries, ConnectUtil.API_VERSION);
				result.put(dmr.getMetadataObjects()[i].getXmlName(),
						queryResult);
				System.out.print(".");
			}
			System.out.println(".");
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return result;
	}

}
