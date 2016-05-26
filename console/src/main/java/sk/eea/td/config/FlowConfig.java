package sk.eea.td.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.activities.*;
import sk.eea.td.rest.model.Connector;

@Configuration
public class FlowConfig {

    @Bean
    public Activity harvestActivity() {
        return new HarvestActivity();
    }

    @Bean
    public Activity transformActivity() {
        return new TransformActivity();
    }

    @Bean
    public Activity storeActivity() {
        return new StoreActivity();
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
    public FlowManager europeanaToHistorypinFlowManager() {
        FlowManager flowManager = new FlowManagerImpl(Connector.EUROPEANA, Connector.HISTORYPIN);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

    @Bean
    public FlowManager historypinOntotextFlowManager() {
        FlowManager flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.SD);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(ontotext2HistorypinTransformActivity());
        return flowManager;
    }

    @Bean
    public FlowManager historypinToEuropeanaFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.MINT);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

}
