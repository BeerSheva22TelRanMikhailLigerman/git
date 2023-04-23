package telran.git;

import java.io.Serializable;
import java.util.Random;
import java.util.stream.Collectors;

public record CommitMessage(String name, String message)  implements Serializable{
	
	static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	static String generateName() {		
		return new Random().ints(7, 0, chars.length()).mapToObj(chars::charAt).map(Object::toString)
				.collect(Collectors.joining());
	}
	public CommitMessage(String message) {
		this (generateName(), message);
		
		
	}
	

}
