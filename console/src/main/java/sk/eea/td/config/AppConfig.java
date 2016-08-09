package sk.eea.td.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import sk.eea.td.flow.FlowManager;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "sk.eea.td")
public class AppConfig implements SchedulingConfigurer {

    @Autowired
    private FlowManager historypinOntotextFlowManager;

    @Autowired
    private FlowManager europeanaToHistorypinFlowManager;

    @Autowired
    private FlowManager historypinToEuropeanaFlowManager;
    
    @Autowired 
    private FlowManager dataflow4;
    
    @Autowired
    private FlowManager dataflow6;
    
    @Autowired
    private FlowManager dataflow6Subflow;
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Schedules(
            //@Scheduled(cron = "${europeana.flm.cron.expression}")
            @Scheduled(fixedRate = 60000)
    )
    public void europeanaToHistorypinTimeSignal() {
        europeanaToHistorypinFlowManager.trigger();
    }

    @Schedules(
            //@Scheduled(cron= "${ontotext.flm.cron.expression}")
            @Scheduled(fixedRate = 60000)
    )
    public void historypinOntotextTimeSignal() {
        historypinOntotextFlowManager.trigger();
    }

    @Schedules(
            //@Scheduled(cron="${historypin.flm.cron.expression}")
            @Scheduled(fixedRate = 60000)
    )
    public void historypinToEuropeanaTimeSignal() {
        historypinToEuropeanaFlowManager.trigger();
    }

    @Schedules(
    		//@Scheduled(cron="${historypinAnnotation.flm.cron.expression}
    		@Scheduled(fixedRate=60000)
	)
    public void dataflow4Trigger(){
    	dataflow4.trigger();
    }

    @Schedules(
            //@Scheduled(cron="${historypinAnnotation.flm.cron.expression}
            @Scheduled(fixedRate=60000)
    )
    public void dataflow6Trigger(){
        dataflow6.trigger();
    }

    @Schedules(
            //@Scheduled(cron="${historypinAnnotation.flm.cron.expression}
            @Scheduled(fixedRate=60000)
    )
    public void dataflow6SubflowTrigger(){
        dataflow6Subflow.trigger();
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
