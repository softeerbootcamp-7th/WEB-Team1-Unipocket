package com.genesis.unipocket;

import org.springframework.boot.SpringApplication;

public class TestUnipocketApplication {

	public static void main(String[] args) {
		SpringApplication.from(UnipocketApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}
}
