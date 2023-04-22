package telran.git;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

public record Commit(	CommitMessage commitMessage,
						Instant commitTime,
						Map<Path, FileParameters> fileParameters,
						String prevCommitName) {}


