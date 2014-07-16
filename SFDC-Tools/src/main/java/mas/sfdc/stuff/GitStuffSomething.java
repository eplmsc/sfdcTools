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
	
}
