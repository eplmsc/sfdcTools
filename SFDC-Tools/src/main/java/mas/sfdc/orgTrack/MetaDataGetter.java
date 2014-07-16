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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

	private static final int BUFFER_SIZE = 4096;
	public static final long ONE_SECOND = 1000;
	public static final int MAX_POLL_REQUEST = 7;

	public static RetrieveResult retrieveAllOrgMetaData(
			String authenticationUrl, String userId, String password) {
		ConnectUtil connectUtil = new ConnectUtil(authenticationUrl, userId,
				password);
		connectUtil.connect();

		RetrieveRequest request = getRetrieveEverythingRequest(connectUtil);
		RetrieveResult result = requestAndPollForMetadata(connectUtil, request);

		return result;
	}

	/**
	 * As simple call to describeMetadata()
	 * 
	 * @param metadataConnection
	 *            a Connection. Must be logged in.
	 * @return The obtained DescribeMetadataResult
	 */
	static public DescribeMetadataResult describeMetadata(
			MetadataConnection metadataConnection) {
		System.out.println("Getting metadata description...");
		DescribeMetadataResult dmr = null;
		try {
			dmr = metadataConnection
					.describeMetadata(ConnectUtil.DEFAULT_API_VERSION);
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return dmr;
	}

	public static RetrieveRequest getRetrieveEverythingRequest(
			ConnectUtil connectUtil) {

		System.out.println("Getting RetreiveRequest for all metadata...");
		MetadataConnection mc = connectUtil.getConnection();
		DescribeMetadataResult dmr = null;
		try {
			dmr = mc.describeMetadata(connectUtil.getApiVersion());
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}

		final String[] asterisk = new String[] { "*" };
		ArrayList<PackageTypeMembers> packageTypeMemberList = new ArrayList<PackageTypeMembers>();
		for (int i = 0; i < dmr.getMetadataObjects().length; i++) {
			if (!"InstalledPackage"
					.equalsIgnoreCase(dmr.getMetadataObjects()[i].getXmlName())) {
				PackageTypeMembers packageTypeMembers = new PackageTypeMembers();
				packageTypeMembers.setName(dmr.getMetadataObjects()[i]
						.getXmlName());
				packageTypeMembers.setMembers(asterisk);
				packageTypeMemberList.add(packageTypeMembers);
			}
		}

		Package unpackaged = new Package();
		unpackaged.setVersion(Double.toString(connectUtil.getApiVersion()));
		unpackaged.setApiAccessLevel(APIAccessLevel.Unrestricted);
		unpackaged.setTypes(packageTypeMemberList
				.toArray(new PackageTypeMembers[packageTypeMemberList.size()]));

		RetrieveRequest request = new RetrieveRequest();
		request.setUnpackaged(unpackaged);
		request.setApiVersion(connectUtil.getApiVersion());

		return request;
	}

	public static RetrieveResult requestAndPollForMetadata(
			ConnectUtil connectUtil, RetrieveRequest request) {

		System.out.println("Issuing retrieve request...");
		MetadataConnection mc = connectUtil.getConnection();

		RetrieveResult result = null;
		AsyncResult asyncResult;
		try {
			asyncResult = mc.retrieve(request);

			// Wait for the retrieve to complete
			int poll = 0;
			long waitTimeMilliSecs = ONE_SECOND;
			while (!asyncResult.isDone()) {
				System.out.println("Polling result again in "
						+ waitTimeMilliSecs / 1000 + " seconds");
				try {
					Thread.sleep(waitTimeMilliSecs);
				} catch (InterruptedException ignored) {
				}
				// double the wait time for the next iteration
				waitTimeMilliSecs *= 2;
				if (poll++ > MAX_POLL_REQUEST) {
					System.out
							.println("Request timed out.  If this is a large set "
									+ "of metadata components, check that the time allowed "
									+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
					System.exit(1);
				}
				asyncResult = mc
						.checkStatus(new String[] { asyncResult.getId() })[0];
				System.out.println("Status is: " + asyncResult.getState());
			}

			if (asyncResult.getState() != AsyncRequestState.Completed) {
				System.out.println("AsyncResult has unexpected statusCode="
						+ asyncResult.getMessage());
			}

			System.out.println("Retrieving result");
			result = mc.checkRetrieveStatus(asyncResult.getId());
		} catch (ConnectionException e) {
			e.printStackTrace();
			System.exit(1);
		}

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

		return result;
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

	public static void saveRetrieveResultAsZip(RetrieveResult result,
			String targetDirectory, String targetFile) {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				result.getZipFile());
		File resultFile = new File(targetDirectory, targetFile);
		try {
			FileOutputStream os = new FileOutputStream(resultFile);
			try {
				ReadableByteChannel src = Channels.newChannel(bais);
				FileChannel dest = os.getChannel();
				copy(src, dest);
				System.out.println("Result written to "
						+ resultFile.getAbsolutePath());
			} finally {
				os.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void saveRetrieveResultAsFiles(RetrieveResult result,
			String outputDirectory) {

		System.out.println("unzipping retrieved result...");

		byte[] buffer = new byte[BUFFER_SIZE];

		try {

			// create output directory (including parents) if it is not already
			// there
			File folder = new File(outputDirectory);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(
					result.getZipFile()));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputDirectory + File.separator
						+ fileName);

				System.out.println("unzipping : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("...done");

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * As simple call to describeMetaData followed by listMetaData
	 * 
	 * @param metadataConnection
	 * @return A Map of metadata types and members
	 */
	static public Map<String, FileProperties[]> getAllMetadataProperties(
			MetadataConnection metadataConnection) {
		System.out.println("Getting metadata...");
		Map<String, FileProperties[]> result = new HashMap<String, FileProperties[]>();
		try {
			DescribeMetadataResult dmr = metadataConnection
					.describeMetadata(ConnectUtil.DEFAULT_API_VERSION);
			ListMetadataQuery[] queries = new ListMetadataQuery[1];
			for (int i = 0; i < dmr.getMetadataObjects().length; i++) {
				queries[0] = new ListMetadataQuery();
				queries[0].setType(dmr.getMetadataObjects()[i].getXmlName());

				FileProperties[] queryResult = metadataConnection.listMetadata(
						queries, ConnectUtil.DEFAULT_API_VERSION);
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
