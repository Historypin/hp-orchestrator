package sk.eea.td.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import sk.eea.td.flow.FlowManager;

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

    @Autowired
    FlowManager historypinOntotextFlowManager;
    @Autowired
    FlowManager europeanaToHistorypinFlowManager;
    @Autowired
    FlowManager historypinToEuropeanaFlowManager;

    @Schedules(
            //@Scheduled(cron = "${europeana.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void europeanaToHistorypinTimeSignal() {
        europeanaToHistorypinFlowManager.trigger();
    }

    @Schedules(@Scheduled(fixedRate = 60000))
    public void historypinOntotextTimeSignal() {
        historypinOntotextFlowManager.trigger();
    }

    @Schedules(
            //@Scheduled(cron="${historypin.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void historypinToEuropeanaTimeSignal() {
        historypinToEuropeanaFlowManager.trigger();
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
