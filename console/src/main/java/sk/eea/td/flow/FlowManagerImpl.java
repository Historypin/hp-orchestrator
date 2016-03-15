package sk.eea.td.flow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.repository.JobRunRepository;

public class FlowManagerImpl implements FlowManager {
	
	@Autowired
	private JobRunRepository processRepository;

	List<Activity> activities = new ArrayList<Activity>();

	private String source;

	/* (non-Javadoc)
	 * @see sk.eea.td.flow.FlowManager#startFlow(sk.eea.td.flow.model.FlowConfig)
	 */
	@Override
	public void startFlow(JobRun context) {
		List<Activity> activities = getActivities();
		try{
			for (Activity activity:activities){
				persistState(context);
				activity.execute(context);
				persistState(context);
			}
			finishFlow(context);
			persistState(context);
		}catch(FlowException e){
			failFlow(context);
			persistState(context);
		}
	}
	
	protected void finishFlow(JobRun context){
		context.setStatus(JobRunStatus.FINISHED);
		context.setResult(JobRunResult.OK);
	}

	protected void failFlow(JobRun context){
		context.setStatus(JobRunStatus.STOPPED);
		context.setResult(JobRunResult.FAILED);
	}
	@Override
	public void persistState(JobRun config){
		processRepository.save(config);
	}

	@Override
	public void startHarvest() {

	}

	public List<Activity> getActivities() {
		return activities;
	}

	public void addActivity(Activity activity) {
		activities.add(activity);
	}

	public void setSource(String source) {
		this.source = source;
	}
}
