package telran.git;

import java.io.Serializable;
import java.nio.file.Path;

public record FileState(	Path path,
							Status status) implements Serializable {

}
