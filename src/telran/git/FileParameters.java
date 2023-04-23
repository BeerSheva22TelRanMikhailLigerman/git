package telran.git;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

public record FileParameters(String[] fileData, Instant timeLastModified) implements Serializable {

}
