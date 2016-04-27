package sk.eea.td.flow.activities;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class SpringTestIntializer implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext applicationContext) {
		System.setProperty("spring.profiles.active","dev");
	}
}
