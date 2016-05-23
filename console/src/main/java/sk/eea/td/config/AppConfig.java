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

    @Schedules(
            //@Scheduled(cron = "${europeana.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void europeanaToHistorypinTimeSignal() {
        europeanaToHistorypinFlowManager().trigger();
    }

    @Bean
    Activity ontotext2HistorypinTransformActivity() {
        return new Ontotext2HistorypinTransformActivity();
    }

    @Bean
    public FlowManager testFlowManager() {
        FlowManager flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.SD);

        Activity a1 = new Activity() {
            @Override
            public void execute(JobRun context) throws FlowException {
                LOG.debug("executing activity: ", getId());
            }
            @Override
            public String getName() {
                return getId();
            }
            @Override
            public String getId() {
                return "A1";
            }
            @Override
            public boolean isSleepAfter() {
                return false;
            }
        };
        Activity a2 = new Activity() {
            @Override
            public void execute(JobRun context) throws FlowException {
                LOG.debug("executing activity: ", getId());
            }
            @Override
            public String getName() {
                return getId();
            }
            @Override
            public String getId() {
                return "A2";
            }
            @Override
            public boolean isSleepAfter() {
                return true;
            }
        };
        Activity a3 = new Activity() {
            @Override
            public void execute(JobRun context) throws FlowException {
                LOG.debug("executing activity: ", getId());
            }
            @Override
            public String getName() {
                return getId();
            }
            @Override
            public String getId() {
                return "A3";
            }
            @Override
            public boolean isSleepAfter() {
                return false;
            }
        };
        Activity a4 = new Activity() {
            @Override
            public void execute(JobRun context) throws FlowException {
                LOG.debug("executing activity: ", getId());
            }
            @Override
            public String getName() {
                return getId();
            }
            @Override
            public String getId() {
                return "A4";
            }
            @Override
            public boolean isSleepAfter() {
                return true;
            }
        };
//        flowManager.addActivity(a1);
//        flowManager.addActivity(a2);
//        flowManager.addActivity(a3);
//        flowManager.addActivity(a4);

        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(ontotext2HistorypinTransformActivity());
        return flowManager;
    }
    @Schedules(@Scheduled(fixedRate = 60000))
    public void testFlowManagerTimeSignal() {
        FlowManager fm = testFlowManager();
        System.out.println(fm);
        System.out.println(fm.getSource());
        fm.trigger();
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

    @Schedules(
            //@Scheduled(cron="${historypin.flm.cron.expression}")
            @Scheduled(fixedRate = 1000)
    )
    public void historypinToEuropeanaTimeSignal() {
        historypinToEuropeanaFlowManager().trigger();
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
