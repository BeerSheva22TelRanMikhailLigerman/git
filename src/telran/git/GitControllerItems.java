package telran.git;

import java.nio.file.Path;
import java.util.*;

import telran.view.*;

public class GitControllerItems {
	GitRepositoryImpl repository;

	public GitControllerItems(GitRepositoryImpl repository) {
		this.repository = repository;
	}

	public Menu menu() {
		return new Menu("Git Repository",
				Item.of("commit", io -> commit(io)),
				Item.of("info", io -> info(io)),
				Item.of("createBranch", io -> createBranch(io)),
				Item.of("renameBranch", io -> renameBranch(io)),
				Item.of("deleteBranch", io -> deleteBranch(io)),
				Item.of("log", io -> log(io)),
				Item.of("branches", io -> branches(io)),
				Item.of("commitContent", io -> commitContent(io)),
				Item.of("switchTo", io -> switchTo(io)),
				Item.of("getHead", io -> getHead(io)),
				Item.of("addIgnoredFileNameExp", io -> addIgnoredFileNameExp(io)),
				Item.of("Exit", io -> {
					repository.save();
					io.writeLine("saved successfully");
				}, true));
	}



	private void commit(InputOutput io) {
		String commitMessage = io.readString("Enter commit message");
		io.writeLine(repository.commit(commitMessage));
	}

	private void info(InputOutput io) {
		List<FileState> res = repository.info();
		res.forEach(io::writeLine);
	}

	private void createBranch(InputOutput io) {
		String branchName = io.readString("Enter new branch name");
		io.writeLine(repository.createBranch(branchName));
	}

	private void renameBranch(InputOutput io) {
		String branchName = io.readString("Enter old branch name");
		String newName = io.readString("Enter new branch name");
		io.writeLine(repository.renameBranch(branchName, newName));
	}

	private void deleteBranch(InputOutput io) {
		String branchName = io.readString("Enter branch name to delete");
		io.writeLine(repository.deleteBranch(branchName));
	}

	private void log(InputOutput io) {
		List<CommitMessage> res = repository.log();
		res.forEach(io::writeLine);
	}

	private void branches(InputOutput io) {
		List<String> res = repository.branches();
		res.forEach(io::writeLine);
	}

	private void commitContent(InputOutput io) {
		String commitName = io.readString("Enter commit name");
		List<Path> res = repository.commitContent(commitName);
		res.forEach(io::writeLine);
	}

	private void switchTo(InputOutput io) {
		String name = io.readString("Enter branch or commit name to switchs");
		io.writeLine(repository.switchTo(name));
	}

	private void getHead(InputOutput io) {
		io.writeLine(repository.getHead());
	}

	private void addIgnoredFileNameExp(InputOutput io) {
		String regex = io.readString("Enter ignored file name");
		io.writeLine(repository.addIgnoredFileNameExp(regex));
	}
}
