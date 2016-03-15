package sk.eea.td.flow;

import java.util.ArrayList;
import java.util.List;

import sk.eea.td.console.model.Process;
import sk.eea.td.console.model.Process.ProcessResult;
import sk.eea.td.console.model.Process.ProcessStatus;

public class FlowManagerImpl implements FlowManager {

	List<Activity> activities = new ArrayList<Activity>();
	/* (non-Javadoc)
	 * @see sk.eea.td.flow.FlowManager#startFlow(sk.eea.td.flow.model.FlowConfig)
	 */
	@Override
	public void startFlow(Process context) {
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
	
	protected void finishFlow(Process context){
		context.setStatus(ProcessStatus.FINISHED);
		context.setResult(ProcessResult.OK);
	}

	protected void failFlow(Process context){
		context.setStatus(ProcessStatus.STOPPED);
		context.setResult(ProcessResult.FAILED);
	}
	@Override
	public void persistState(Process config){
		
	}
		
	public List<Activity> getActivities() {
		return activities;
	}

	public void addActivity(Activity activity) {
		activities.add(activity);
	}
}
