package mas.sfdc.stuff;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitStuffSomething {
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

	// public void describeMetadata() {
	// try {
	// double apiVersion = 21.0;
	// // Assuming that the SOAP binding has already been established.
	// DescribeMetadataResult res =
	// metadataConnection.describeMetadata(apiVersion);
	// StringBuffer sb = new StringBuffer();
	// if (res != null && res.getMetadataObjects().length > 0) {
	// for (DescribeMetadataObject obj : res.getMetadataObjects()) {
	// sb.append("***************************************************\n");
	// sb.append("XMLName: " + obj.getXmlName() + "\n");
	// sb.append("DirName: " + obj.getDirectoryName() + "\n");
	// sb.append("Suffix: " + obj.getSuffix() + "\n");
	// sb.append("***************************************************\n");
	// }
	// } else {
	// sb.append("Failed to obtain metadata types.");
	// }
	// System.out.println(sb.toString());
	// } catch (ConnectionException ce) {
	// ce.printStackTrace();
	// }
	// }

	// public void outputChangeReport(
	// Map<String, FileProperties[]> metadataPropertiesMap) {
	//
	// int maxWidthType = 0;
	// int maxWidthName = 0;
	// for (String elementName: metadataPropertiesMap.keySet()) {
	// for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
	// if (fp.getLastModifiedDate().after(this.afterDate)) {
	// if (fp.getType().length() > maxWidthType) {
	// maxWidthType = fp.getType().length();
	// }
	// if (fp.getFullName().length() > maxWidthName) {
	// maxWidthName = fp.getFullName().length();
	// }
	// }
	// }
	// }
	//
	// maxWidthName += 4;
	// maxWidthType += 4;
	//
	// String formatString =
	// "%-"+maxWidthType+"s %-"+maxWidthName+"s %-20s %s%n";
	//
	// for (String elementName: metadataPropertiesMap.keySet()) {
	// int breakpoint=0;breakpoint=breakpoint;
	// for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
	// if (fp.getLastModifiedDate().after(this.afterDate)) {
	// System.out.format(formatString
	// ,fp.getType()
	// ,fp.getFullName()
	// ,outputDateFormat.format(fp.getLastModifiedDate().getTime())
	// ,fp.getLastModifiedByName());
	// }
	// }
	// }
	// }

	
}
