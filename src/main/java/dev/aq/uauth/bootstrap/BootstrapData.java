package dev.aq.uauth.bootstrap;

import dev.aq.uauth.authentication.AuthenticationService;
import dev.aq.uauth.constants.Role;
import dev.aq.uauth.dto.SignUpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;


public class BootstrapData implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(BootstrapData.class);

  private final AuthenticationService authenticationService;

  public BootstrapData(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Override
  public void run(String... args) throws Exception {

    SignUpRequest admin = SignUpRequest.builder()
        .firstName("Admin")
        .lastName("Admin")
        .email("admin@mail.com")
        .password("p@ssword")
        .role(Role.ADMIN)
        .build();

    logger.info("Admin token: {}", authenticationService.register(admin).getAccessToken());

    SignUpRequest manager = SignUpRequest.builder()
        .firstName("Manager")
        .lastName("Manager")
        .email("manager@mail.com")
        .password("passw0rd")
        .role(Role.MANAGER)
        .build();

    logger.info("Manager token: {}", authenticationService.register(manager).getAccessToken());

    SignUpRequest user = SignUpRequest.builder()
        .firstName("User")
        .lastName("user")
        .email("user@mail.com")
        .password("pa$$word")
        .role(Role.USER)
        .build();

    logger.info("User token: {}", authenticationService.register(user).getAccessToken());

  }
}
