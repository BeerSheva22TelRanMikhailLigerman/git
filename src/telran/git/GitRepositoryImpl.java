package telran.git;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
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
			// head = commit.commitMessage().name();
			res = "Commited succefull: " + commit.commitMessage().message() + " comName: "
					+ commit.commitMessage().name();

			if (head == null) {
				head = commit.commitMessage().name();
				createBranch("master");
			} else {
				branches.replace(head, commit.commitMessage().name());
			}
		}

		return res;
	}

	private Commit getCommit(String commitMessage, List<FileState> fileStates) { // create new commit
		CommitMessage comMessage = new CommitMessage(commitMessage);
		Instant comTime = Instant.now();
		Map<Path, FileParameters> comFileParameters = getFileParameters(fileStates);
		String prevCommit = head == null ? null : getCurrentCommit(head).commitMessage().name();

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
					.map(filePath -> new FileState(filePath, getFileStatus(filePath, getCurrentCommit(head)))).toList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return res;
	}

	private Status getFileStatus(Path path, Commit commit) {
		Status res = null;
		if (commit == null) {
			res = Status.UNTRACKED;
		} else if (!commit.fileParameters().containsKey(path)) {
			res = getFileStatus(path, commits.get(commit.prevCommitName()));
		} else {
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
	public String createBranch(String newBranchName) {
		String res = null;

		if (commits.isEmpty()) {
			res = "Must be at least one commit";

		} else if (branches.containsKey(newBranchName)) {
			res = String.format("Branch  %s already exists", newBranchName);
		}
		branches.put(newBranchName, getCurrentCommit(head).commitMessage().name());
		head = newBranchName;
		res = String.format("Branch %s has been created", newBranchName);

		return res;
	}

	private Commit getCurrentCommit(String name) { // return commit by name
		Commit res = null;
		// res = commits.get(name);
		if (name == null) {
			return null;
		}
		if (branches.containsKey(name)) {
			res = commits.get(branches.get(name));
		} else {
			res = commits.get(name);
		}
		return res;
	}

	@Override
	public String renameBranch(String branchName, String newBranchName) {
		String res = null;
		if (branches.containsKey(newBranchName)) {
			res = String.format("Branch  %s already exists", newBranchName);
		}
		if (!branches.containsKey(branchName)) {
			res = String.format("Branch  %s  not found", branchName);
		}

		String commitName = branches.remove(branchName);
		branches.put(newBranchName, commitName);
		res = "Renamed successfully";

		if (head == branchName) {
			head = newBranchName;
		}

		return res;
	}

	@Override
	public String deleteBranch(String branchName) {
		String res = null;
		if (!branches.containsKey(branchName)) {
			res = "No such branch faund";
		} else if (head == branchName) {
			res = "Cann't delete current branch";
		} else {
			branches.remove(branchName);
			res = "Delete successfully";
		}
		return res;
	}

	@Override
	public List<CommitMessage> log() {
		List<CommitMessage> res = new ArrayList<>();
		Commit commit = getCurrentCommit(head);
		while (commit != null) {
			res.add(commit.commitMessage());
			commit = commits.get(commit.prevCommitName());
		}
		return res;
	}

	@Override
	public List<String> branches() {
		return branches.keySet().stream().map(name -> name == head ? name + " *" : name).toList();
	}

	@Override
	public List<Path> commitContent(String commitName) {

		return commits.get(commitName).fileParameters().keySet().stream().toList();
	}

	@Override
	public String switchTo(String name) {

		List<FileState> fileStates = info();
		for (FileState state : fileStates) {
			if ((state.status() == Status.MODIFIED) || ((state.status() == Status.UNTRACKED))) {
				return "Need commit first";
			}
		}

		clearWorkDir();
		restoreFiles(name);
		head = name;

		return "switch successfully";
	}

	private void restoreFiles(String name) {
		Commit commit = getCurrentCommit(name);
		var fileParameters = commit.fileParameters();
		for (Entry<Path, FileParameters> pair : fileParameters.entrySet()) {
			Path path = pair.getKey();
			String[] fileData = pair.getValue().fileData();
			Instant timeOfModified = pair.getValue().timeLastModified();

			restoreOneFile(path, fileData, timeOfModified);
		}

		while (commit.prevCommitName() != null) {
			commit = getCurrentCommit(commit.prevCommitName());
			List<Path> filesPath = null;
			try {
				filesPath = Files.walk(Path.of(WORK_DIR), 1).filter(Files::isRegularFile)
						.filter(filePath -> !regexMatches(filePath.getFileName().toString())).toList();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			
			fileParameters = commit.fileParameters();
			for (Entry<Path, FileParameters> pair : fileParameters.entrySet()) {
				Path path = pair.getKey();
				String[] fileData = pair.getValue().fileData();
				Instant timeOfModified = pair.getValue().timeLastModified();
				
				if(!filesPath.contains(path)) {
					restoreOneFile(path, fileData, timeOfModified);
				}
				
			}

		}
		
		
	}

	

	private void restoreOneFile(Path path, String[] fileData, Instant timeOfModified) {
		try {
			Files.createFile(path);
			Files.write(path, Arrays.asList(fileData));
			Files.setLastModifiedTime(path, FileTime.from(timeOfModified));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearWorkDir() {
		try {
			Files.walk(Path.of(WORK_DIR)).filter(Files::isRegularFile).forEach(file -> {
				try {
					Files.delete(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

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
