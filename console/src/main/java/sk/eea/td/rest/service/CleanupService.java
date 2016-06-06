/**
 * 
 */
package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.util.PathUtils;

/**
 * Service 
 * @author Maros Strmensky
 *
 */

@Component
public class CleanupService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CleanupService.class);

    @Value("${storage.directory}")
    private String outputDirectory;

	private final class CleanFilesTask implements Runnable {
		
		private final List<JobRun> jobRuns;
		
		public CleanFilesTask(List<JobRun> jobRuns) {
			this.jobRuns = jobRuns;
		}
		
		@Override
		public void run() {
			for(JobRun jobRun : jobRuns){
				Path jobRunPath = PathUtils.getJobRunPath(Paths.get(outputDirectory), String.valueOf(jobRun.getId()));
				if(jobRunPath.toFile().exists()){
					LOG.debug("Deleting working directory for {}", jobRunPath);
					try {
						FileUtils.deleteDirectory(jobRunPath.toFile());
					} catch (IOException e) {
						LOG.warn("Working directory for {} could not be deleted. Please delete it manually.", jobRunPath);
					}
				}else{
					LOG.debug("Working directory for {} wasn't found. Skipping.", jobRunPath);					
				}
			}
		}
		
		public void start(){
			Thread thread = new Thread(this);
			thread.start();
		}
	}

	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobRunRepository jobRunRepository;
	
	/**
	 * Cleanup JobRun working paths.
	 * @param jobRuns
	 */
	public void cleanUp(List<JobRun> jobRuns) {
		new CleanFilesTask(jobRuns).start();
	}
	
	/**
	 * Delete Job and cleanup files.
	 * @param job
	 */
	public void delete(Job job) {
		List<JobRun> jobRuns = jobRunRepository.findByJob(job);
		cleanUp(jobRuns);
		jobRepository.delete(job);
	}
}
