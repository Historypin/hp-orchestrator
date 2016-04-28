package sk.eea.td.config;

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
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.activities.HarvestActivity;
import sk.eea.td.flow.activities.ReportActivity;
import sk.eea.td.flow.activities.StoreActivity;
import sk.eea.td.flow.activities.TransformActivity;
import sk.eea.td.rest.model.Connector;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@PropertySource({"classpath:default.properties", "classpath:${spring.profiles.active:prod}.properties"})
@ComponentScan(basePackages = "sk.eea.td")
public class AppConfig implements SchedulingConfigurer {

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
    public FlowManager europeanaFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.EUROPEANA, Connector.OAIPMH);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

    @Schedules(
            //@Scheduled(cron = "${europeana.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void europeanaFlowManagerTimeSignal() {
        europeanaFlowManager().trigger();
    }

    @Bean
    public FlowManager historypinFlowManager() {
        FlowManagerImpl flowManager = new FlowManagerImpl(Connector.HISTORYPIN);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(transformActivity());
        flowManager.addActivity(storeActivity());
        flowManager.addActivity(reportActivity());
        return flowManager;
    }

    @Schedules(
            //@Scheduled(cron="${historypin.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void historypinFlowManagerTimeSignal() {
        historypinFlowManager().trigger();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }
}
