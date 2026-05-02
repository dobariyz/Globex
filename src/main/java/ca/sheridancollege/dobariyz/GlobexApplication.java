package ca.sheridancollege.dobariyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GlobexApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlobexApplication.class, args);
	}

}
