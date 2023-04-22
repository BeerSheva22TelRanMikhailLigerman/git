package telran.git;

import java.nio.file.Path;

public record FileState(	Path path,
							Status status) {

}
