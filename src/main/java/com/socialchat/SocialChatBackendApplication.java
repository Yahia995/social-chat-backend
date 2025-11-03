package com.socialchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialChatBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialChatBackendApplication.class, args);
	}

}
