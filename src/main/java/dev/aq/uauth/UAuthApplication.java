package dev.aq.uauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UAuthApplication {

  static final Logger logger = LoggerFactory.getLogger(UAuthApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(UAuthApplication.class, args);
    logger.info("Application started");
  }

}
