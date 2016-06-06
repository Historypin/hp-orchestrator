package sk.eea.td.flow.activities;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.CleanupService;

/**
 * Activity used for cleaning up data.
 * @author Maros Strmensky
 *
 */
@Component
public class CleanupActivity implements Activity {
	
	@Autowired
	CleanupService cleanupService;
	
	@Override
	public ActivityAction execute(JobRun context) throws FlowException {
		cleanupService.cleanUp(Arrays.asList(context));
		return ActivityAction.CONTINUE;
	}
	
	@Override
	public String getName() {
		return "Cleanup activity";
	}
}
