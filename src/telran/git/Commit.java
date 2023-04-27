package telran.git;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

public record Commit(	CommitMessage commitMessage,
						Instant commitTime,
						Map<String, FileParameters> fileParameters,
						String prevCommitName) implements Serializable{}


