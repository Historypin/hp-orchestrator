package sk.eea.td.config;

import java.time.Period;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sk.eea.td.console.model.Connector;
import sk.eea.td.flow.Dataflow4JobSelector;
import sk.eea.td.flow.Dataflow6SubflowSelector;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.JobSelector;
import sk.eea.td.flow.SingleRunJobSelector;
import sk.eea.td.flow.activities.Activity;
import sk.eea.td.flow.activities.Approval2EU_ATransformActivity;
import sk.eea.td.flow.activities.ApprovalSendMailActivity;
import sk.eea.td.flow.activities.CleanupActivity;
import sk.eea.td.flow.activities.Dataflow4isFinalActivity;
import sk.eea.td.flow.activities.EU2TagAppTransformActivity;
import sk.eea.td.flow.activities.FinishFlowActivity;
import sk.eea.td.flow.activities.HP_A2EU_ATransformActivity;
import sk.eea.td.flow.activities.HarvestActivity;
import sk.eea.td.flow.activities.Ontotext2HistorypinTransformAndStoreActivity;
import sk.eea.td.flow.activities.PrepareCSVActivity;
import sk.eea.td.flow.activities.ReportActivity;
import sk.eea.td.flow.activities.SleepActivity;
import sk.eea.td.flow.activities.StoreActivity;
import sk.eea.td.flow.activities.Tagapp2ApproveTransformActivity;
import sk.eea.td.flow.activities.TagappHarvestActivity;
import sk.eea.td.flow.activities.TagappStoreActivity;
import sk.eea.td.flow.activities.TransformActivity;

@Configuration
public class FlowConfig {
    
    @Value("${arttag.harvest.period}")
    private String arttagHarvestPeriod;
	
	@Bean
	public Activity cleanupActivity(){
		return new CleanupActivity();
	}

    @Bean
    public Activity harvestActivity() {
        return new HarvestActivity();
    }

    @Bean
    public Activity transformActivity() {
        return new TransformActivity();
    }

    @Bean
	public HP_A2EU_ATransformActivity hp_a2eu_ATransformActivity() {
		return new HP_A2EU_ATransformActivity();
	}

    @Bean
    public Activity tagapp2hpTransformActivity() {
        return new Tagapp2ApproveTransformActivity();
    }

    @Bean
    public Activity storeActivity() {
        return new StoreActivity();
    }

    @Bean
    public Activity tagappStoreActivity() {
        return new TagappStoreActivity();
    }

    @Bean
    public Activity reportActivity() {
        return new ReportActivity();
    }

    @Bean
    public Activity approvalSendMailActivity() {
        return new ApprovalSendMailActivity();
    }

    @Bean
    public Activity ontotext2HistorypinTransformAndStoreActivity() {
        return new Ontotext2HistorypinTransformAndStoreActivity();
    }

    @Bean
    public JobSelector singleRunJobSelector(){
    	return new SingleRunJobSelector();
    }
    
    @Bean
    public JobSelector dataflow4JobSelector(){
    	return new Dataflow4JobSelector();
    }
    
    @Bean
    public JobSelector dataflow6SubflowSelector(){
        return new Dataflow6SubflowSelector(Period.parse(arttagHarvestPeriod));
    }

    @Bean
    public Activity dataflow4isFinalActivity(){
    	return new Dataflow4isFinalActivity();
    }
    
    @Bean
    public Activity eu2tagAppTransformActivity() {
        return new EU2TagAppTransformActivity();
    }
    
    @Bean
    public Activity prepareCSVActivity() {
        return new PrepareCSVActivity();
    }

    @Bean
    public FlowManager europeanaToHistorypinFlowManager() {
        FlowManager flowManager = new FlowManagerImpl(Connector.EUROPEANA, Connector.HISTORYPIN, singleRunJobSelector());
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

    @Bean
    public FlowManager historypinOntotextFlowManager() {
        FlowManager flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.SD, singleRunJobSelector());
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(ontotext2HistorypinTransformAndStoreActivity());
        //flowManager.addActivity(approvalSendMailActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

    @Bean
    public FlowManager historypinToEuropeanaFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.MINT, singleRunJobSelector());
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }
    
    @Bean
    public FlowManager dataflow4(){
    	FlowManagerImpl flowManager = new FlowManagerImpl(Connector.HISTORYPIN_ANNOTATION, Connector.EUROPEANA_ANNOTATION, dataflow4JobSelector());
    	flowManager.addActivity(harvestActivity());
    	flowManager.addActivity(hp_a2eu_ATransformActivity());
    	flowManager.addActivity(storeActivity());
    	flowManager.addActivity(cleanupActivity());
    	flowManager.addActivity(reportActivity());
    	flowManager.addActivity(dataflow4isFinalActivity());
    	return flowManager;
    }
    
    @Bean
    public FlowManager dataflow6(){
        FlowManager flowManager = new FlowManagerImpl(Connector.EUROPEANA, Connector.TAGAPP, singleRunJobSelector());
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(eu2tagAppTransformActivity());
        flowManager.addActivity(tagappStoreActivity());
        flowManager.addActivity(cleanupActivity());
        flowManager.addActivity(new SleepActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }
    
    @Bean
    public FlowManager dataflow6Subflow(){
        FlowManager flowManager = new FlowManagerImpl(Connector.TAGAPP, Connector.EUROPEANA_ANNOTATION, dataflow6SubflowSelector());
        flowManager.addActivity(tagappHarvestActivity());
        flowManager.addActivity(tagapp2hpTransformActivity());
        flowManager.addActivity(approvalSendMailActivity());        
        flowManager.addActivity(prepareCSVActivity());
        flowManager.addActivity(approval2eu_ATransformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        flowManager.addActivity(cleanupActivity());
        flowManager.addActivity(finishFlowActivity());
        return flowManager;
    }

    @Bean
    public Activity finishFlowActivity() {
        return new FinishFlowActivity();
    }

    @Bean
    public Activity tagappHarvestActivity() {
        return new TagappHarvestActivity();
    }

    @Bean
    public Activity approval2eu_ATransformActivity() {
        return new Approval2EU_ATransformActivity();
    }

}
