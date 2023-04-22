package telran.git;

import telran.view.*;

public class GitConsoleAppl {

	public static void main(String[] args) {
		StandardInputOutput io = new StandardInputOutput();
		GitRepositoryImpl repository = new GitRepositoryImpl();
		GitControllerItems gitController = new GitControllerItems(repository);
		Item menu = gitController.menu();
		menu.perform(io);

	}

}
