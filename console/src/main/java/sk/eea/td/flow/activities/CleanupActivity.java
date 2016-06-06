package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.PathUtils;

/**
 * Activity used for cleaning up data.
 * @author Maros Strmensky
 *
 */
@Component
public class CleanupActivity implements Activity {
	
    @Value("${storage.directory}")
    private String outputDirectory;

	@Override
	public ActivityAction execute(JobRun context) throws FlowException {
		Path jobPath = PathUtils.getJobRunPath(Paths.get(outputDirectory), String.valueOf(context.getId()));
		try {
			FileUtils.deleteDirectory(jobPath.toFile());
		} catch (IOException e) {
			throw new FlowException("Could not delete flow directory",e);
		}
		return ActivityAction.CONTINUE;
	}
	
	@Override
	public String getName() {
		return "Cleanup activity";
	}
}
