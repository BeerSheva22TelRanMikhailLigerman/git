package telran.git;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.PatternSyntaxException;

public class GitRepositoryImpl implements GitRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String WORK_DIR = "D:\\myGitFolder";

	private String head = null;

	HashMap<String, String> branches = new HashMap<>();
	public HashMap<String, Commit> commits = new HashMap<>();
	private Set<String> ignoredFiles = new HashSet<>();

	@Override
	public String commit(String commitMessage) {
		String res = "All files are commited now";
		boolean isChanged = false;
		List<FileState> fileStates = info();

		for (FileState state : fileStates) {
			if ((state.status() == Status.MODIFIED) || ((state.status() == Status.UNTRACKED))) {
				isChanged = true;
			}
		}

		if (isChanged) {
			Commit commit = getCommit(commitMessage, fileStates);
			commits.put(commit.commitMessage().name(), commit);
			head = commit.commitMessage().name();
			res = "Commited succefull: " + commit.commitMessage().message() + " comName: " + commit.commitMessage().name();
		}

		return res;
	}

	private Commit getCommit(String commitMessage, List<FileState> fileStates) {
		CommitMessage comMessage = new CommitMessage(commitMessage);
		Instant comTime = Instant.now();
		Map<Path, FileParameters> comFileParameters = getFileParameters(fileStates);
		String prevCommit = head;

		return new Commit(comMessage, comTime, comFileParameters, prevCommit);
	}

	private Map<Path, FileParameters> getFileParameters(List<FileState> fileStates) {
		Map<Path, FileParameters> res = new HashMap<>();
		for (FileState fileState : fileStates) {
			if (fileState.status() != Status.COMMITED) {
				FileParameters fileParameters = new FileParameters(getData(fileState.path()),
																	fileLastModified(fileState.path()));
				res.put(fileState.path(), fileParameters);
			}		
		}

		return res;
	}

	private String[] getData(Path path) {
		String[] res = null;
		try {
			res = Files.readAllLines(path).toArray(String[]::new);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public List<FileState> info() {
		Path directory = Path.of(WORK_DIR);
		List<FileState> res = null;
		try {
			res = Files.walk(directory, 1).filter(Files::isRegularFile)
					.filter(filePath -> !regexMatches(filePath.getFileName().toString()))
					.map(filePath -> new FileState(filePath, getFileStatus(filePath, commits.get(head)))).toList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return res;
	}

	private Status getFileStatus(Path path, Commit commit) {		
		Status res = null;		
		if (commit == null) {
			res = Status.UNTRACKED;
		} 
		else if (!commit.fileParameters().containsKey(path)) {				
			res = getFileStatus(path, commits.get(commit.prevCommitName()));
		} 
		else {
			Instant commitDate = commit.commitTime();
			res = commitDate.isBefore(fileLastModified(path)) ? Status.MODIFIED : Status.COMMITED;
		}
		return res;
	}
	

	private Instant fileLastModified(Path path) {
		return Instant.ofEpochMilli(path.toFile().lastModified());
	}

	private boolean regexMatches(String fileName) {
		return ignoredFiles.stream().anyMatch(fileName::matches);
	}

	@Override
	public String createBranch(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String renameBranch(String branchName, String newName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteBranch(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommitMessage> log() {
		List<CommitMessage> res = new ArrayList<>();
		Commit commit = commits.get(head);
		while (commit != null) {
			res.add(commit.commitMessage());
			commit = commits.get(commit.prevCommitName());
		}
		return res;
	}

	@Override
	public List<String> branches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Path> commitContent(String commitName) {		
		return commits.get(commitName).fileParameters().keySet().stream().toList();
	}

	@Override
	public String switchTo(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHead() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save() {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(GIT_FILE))) {
			output.writeObject(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String addIgnoredFileNameExp(String regex) {
		String checker = "checker";
		try {
			checker.matches(regex);
			ignoredFiles.add(regex);
		} catch (PatternSyntaxException e) {
			// TODO Auto-generated catch block
		}
		return String.format("Ignored Expression %s was added", regex);
	}

	// try to load saved repository or create new empty repository
	public static GitRepositoryImpl init() {
		File file = new File(GIT_FILE);
		GitRepositoryImpl repository = new GitRepositoryImpl();
		if (file.exists()) {
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
				repository = (GitRepositoryImpl) input.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return repository;
	}

}
