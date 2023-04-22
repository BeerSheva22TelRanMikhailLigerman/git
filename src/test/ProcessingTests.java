package test;


import java.util.*;

import telran.git.*;

public class ProcessingTests {
	public static void main(String[] args) throws InterruptedException {
	//	testInfo();
		GitRepositoryImpl rep = GitRepositoryImpl.init();
		
		rep.addIgnoredFileNameExp("2.txt");
		testInfo(rep);
//		System.out.println("First round");
//		testInfo(rep);
//		String res = rep.commit("My first commit");
//		System.out.println(res);		
//		System.out.println(rep.commits.size());		
//		Thread.sleep(5000);
//		
//		System.out.println("Second round");		
//		testInfo(rep);
//		res = rep.commit("My second commit");
//		System.out.println(res);
//		System.out.println(rep.commits.size());
//		
//		//rep.save();
//		Thread.sleep(5000);
//		testInfo(rep);
//		System.out.println("Third round");
//		res = rep.commit("My Third commit");
//		System.out.println(res);
//		
//		List<CommitMessage> list = rep.log();
//		for (CommitMessage comMessage: list) {
//			System.out.println(comMessage.name() + " - " + comMessage.message());
//		}
//		
//		System.out.println("commitContent");
		

	}

	public static void testInfo(GitRepositoryImpl rep) {
		//GitRepositoryImpl rep = GitRepositoryImpl.init();
		List<FileState> list = rep.info();
		for (FileState state : list) {
			System.out.println(state.path() + ": " + state.status());
		}
	}

}
