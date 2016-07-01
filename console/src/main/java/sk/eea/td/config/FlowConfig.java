package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sk.eea.td.flow.Dataflow4JobSelector;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.JobSelector;
import sk.eea.td.flow.SingleRunJobSelector;
import sk.eea.td.flow.activities.*;
import sk.eea.td.rest.model.Connector;

@Configuration
public class FlowConfig {
	
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
    public Activity ontotext2HistorypinTransformActivity() {
        return new Ontotext2HistorypinTransformActivity();
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
    public Activity dataflow4isFinalActivity(){
    	return new Dataflow4isFinalActivity();
    }
    
    @Bean
    public Activity eu2tagAppTransformActivity() {
        return new EU2TagAppTransformActivity();
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
        flowManager.addActivity(ontotext2HistorypinTransformActivity());
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
        flowManager.addActivity(reportActivity());
        return flowManager;
    }
    
//    @Bean
//    public FlowManager dataflow6Subflow(){
//        FlowManager flowManager = new FlowManagerImpl(Connector.TAGAPP, Connector.EUROPEANA, dataflow6SubflowSelector());
//        flowManager.addActivity(harvestActivity());
//        flowManager.addActivity(tagapp2hpTransformActivity());
//        flowManager.addActivity(reportActivity());
//        flowManager.addActivity(hp2eu_ATransformActivity());
//        flowManager.addActivity(cleanupActivity());
//        flowManager.addActivity(reportActivity());
//        flowManager.addActivity(dataflow6SubflowIsFinalActivity());
//        return flowManager;
//    }

}
