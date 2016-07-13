/**
 * 
 */
package sk.eea.td.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobSubRun;

/**
 * @author Maros Strmensky
 *
 */
public class SubFlowManagerImpl extends FlowManagerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SubFlowManagerImpl.class);
    
    public SubFlowManagerImpl(Connector source, Connector target, JobSelector jobSelector) {
        super(source, target, jobSelector);
    }
    
    @Override
    public void resumeFlow(JobRun context) {
        JobSubRun subRun = new JobSubRun();
        subRun.setJob(context.getJob());
        subRun.setParentRun(context);
        
        super.resumeFlow(subRun);
    }    
}
