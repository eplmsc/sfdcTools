package mas.sfdc.orgTrack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
	private static final String OUTPUT_DIRECTORY = null;

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

			System.out.println("Retrieving result");
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
			saveRetrieveResult(result);
			unzipRetrieveResult(result, "c:\\data\\sletmig\\sfdctool\\test");
			Repository repo = obtainLocalGitRepo("c:\\data\\sletmig\\sfdctool\\test\\.git");
			addAndCommitGit(repo, "unpackaged");
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

	private static void saveRetrieveResult(RetrieveResult result) {
		ByteArrayInputStream bais = new ByteArrayInputStream(result.getZipFile());
		File resultFile = new File("SFDCTools.retrieveResults.zip");
		try {
			FileOutputStream os = new FileOutputStream(resultFile);
			try {
				ReadableByteChannel src = Channels.newChannel(bais);
				FileChannel dest = os.getChannel();
				copy(src, dest);
				System.out.println("Result written to "	+ resultFile.getAbsolutePath());
			} finally {
				os.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void addAndCommitGit(Repository repo, String outputDirectory) {
		Git git = new Git(repo);
		System.out.println("repo worktree path"+repo.getWorkTree().getAbsolutePath());
		try {
			DirCache dc = git.add().addFilepattern(outputDirectory).call();
			System.out.println("dc.getEntryCount()="+dc.getEntryCount());
			git.commit().setMessage("test message").call();
		} catch (NoFilepatternException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GitAPIException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	private static Repository obtainLocalGitRepo(String repoDirectory) {
		Repository result = null;
		System.out.println("Obtaining Git Repository...");
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			File repoDir = new File(repoDirectory);
			result = builder.setGitDir(repoDir)
			  .readEnvironment() // scan environment GIT_* variables
			  .findGitDir() // scan up the file system tree
			  .setup()
			  .build();
			if (!repoDir.exists()) result.create();
			System.out.println("repo directory is "+result.getDirectory().getAbsolutePath());			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
		System.out.println("...done");
		return result;
	}
	
	private static void unzipRetrieveResult(RetrieveResult result,
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
