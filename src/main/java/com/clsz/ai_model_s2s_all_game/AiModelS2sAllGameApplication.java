package com.clsz.ai_model_s2s_all_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


//测试
@EnableScheduling
@SpringBootApplication
@EnableAsync
public class AiModelS2sAllGameApplication {


	public static void main(String[] args) {
		SpringApplication.run(AiModelS2sAllGameApplication.class, args);
	}

}
