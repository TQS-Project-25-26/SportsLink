package tqs.sportslink;

import org.springframework.boot.SpringApplication;

public class TestSportslinkApplication {

	public static void main(String[] args) {
		SpringApplication.from(SportslinkApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
