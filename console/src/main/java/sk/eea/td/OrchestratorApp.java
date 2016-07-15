package sk.eea.td;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PropertySource({"classpath:default.properties", "file:${working.directory}/conf/orchestrator.properties"})
public class OrchestratorApp extends SpringBootServletInitializer {


    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OrchestratorApp.class);
    }
}
