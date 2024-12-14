package dev.aq.uauth.auth;

import dev.aq.uauth.dto.LogInRequest;
import dev.aq.uauth.dto.LogInResponse;
import dev.aq.uauth.dto.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<LogInResponse> register(@RequestBody SignUpRequest signUpRequest) {
    logger.info("Request for registration: {}", signUpRequest);
    return ResponseEntity.ok(authenticationService.register(signUpRequest));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<LogInResponse> authenticate(@RequestBody LogInRequest logInRequest) {
    logger.info("Request for authentication: {}", logInRequest);
    return ResponseEntity.ok(authenticationService.authenticate(logInRequest));
  }

  @PostMapping("/refresh")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    logger.info("Request for refreshToken: {} {}", request, response);
    authenticationService.refreshToken(request, response);
  }

}
