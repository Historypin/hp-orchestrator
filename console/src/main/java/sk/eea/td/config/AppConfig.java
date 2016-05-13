package sk.eea.td.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.activities.HarvestActivity;
import sk.eea.td.flow.activities.Ontotext2HistorypinTransformActivity;
import sk.eea.td.flow.activities.ReportActivity;
import sk.eea.td.flow.activities.StoreActivity;
import sk.eea.td.flow.activities.TransformActivity;
import sk.eea.td.rest.model.Connector;

@Configuration
@EnableScheduling
@PropertySource({"classpath:default.properties", "classpath:${spring.profiles.active:prod}.properties"})
@ComponentScan(basePackages = "sk.eea.td")
public class AppConfig implements SchedulingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
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
    public Activity storeActivity() {
        return new StoreActivity();
    }

    @Bean
    public Activity reportActivity() {
        return new ReportActivity();
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

//    @Schedules(
//            //@Scheduled(cron = "${europeana.flm.cron.expression}")
//            @Scheduled(fixedRate = 1000)
//    )
//    public void europeanaToHistorypinTimeSignal() {
//        europeanaToHistorypinFlowManager().trigger();
//    }

    @Bean
    Activity ontotext2HistorypinTransformActivity() {
        return new Ontotext2HistorypinTransformActivity();
    }

//    @Bean
//    public FlowManager historypinHarvester(){
//    	FlowManagerImpl flowManager = new FlowManagerImpl("HP");
//        flowManager.addActivity(new HarvestActivity());
//        flowManager.addActivity(new TransformActivity());
//        flowManager.addActivity(new StoreActivity());
//        return flowManager;
//    }
//
//    @Schedules(@Scheduled(cron="${historypin.flm.cron.expression}"))
//    public void historypinFlowManagerTimeSignal(){
//        historypinHarvester().trigger();
//    }

    @Bean
    public FlowManager historypinToEuropeanaFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.EUROPEANA);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

//    @Schedules(
//            //@Scheduled(cron="${historypin.flm.cron.expression}")
//            @Scheduled(fixedRate = 1000)
//    )
//    public void historypinToEuropeanaTimeSignal() {
//        historypinToEuropeanaFlowManager().trigger();
//    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }
}
