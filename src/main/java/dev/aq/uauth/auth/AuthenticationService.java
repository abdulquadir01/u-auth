package dev.aq.uauth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aq.uauth.config.JwtService;
import dev.aq.uauth.constants.TokenType;
import dev.aq.uauth.constants.UAuthConstants;
import dev.aq.uauth.dto.LogInRequest;
import dev.aq.uauth.dto.LogInResponse;
import dev.aq.uauth.dto.SignUpRequest;
import dev.aq.uauth.entity.Token;
import dev.aq.uauth.entity.User;
import dev.aq.uauth.repository.TokenRepository;
import dev.aq.uauth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authManager;
  private final PasswordEncoder passwordEncoder;
  private final TokenRepository tokenRepository;

  public LogInResponse register(SignUpRequest signUpRequest) {
    logger.info("Inside  register()");

    User user = User.builder().id(1L).firstName(signUpRequest.getFirstName()).lastName(signUpRequest.getLastName()).email(signUpRequest.getEmail()).password(passwordEncoder.encode(signUpRequest.getPassword())).role(signUpRequest.getRole()).build();
    logger.info("Saving new User to db: {}", user);
    User savedUser = userRepository.save(user);

    logger.info("Generating Access Token");
    String accessToken = jwtService.generateToken(savedUser);
    logger.info("accessToken: {}", accessToken);
    logger.info("Generating Refresh Token");
    String refreshToken = jwtService.generateRefreshToken(savedUser);
    logger.info("refreshToken: {}", refreshToken);

    /**
     * System.out.println("From AuthService  register() ------");
     System.out.println("accessToken: " + accessToken);
     System.out.println("refreshToken: " + refreshToken);
     */

    Token savedToken = saveUserToken(savedUser, accessToken, refreshToken);
    logger.info("Token saved in db");

    return LogInResponse.builder().username(signUpRequest.getEmail()).accessToken(accessToken).refreshToken(refreshToken).accessExpiresIn(savedToken.getExpiresIn()).build();
  }

  public LogInResponse authenticate(LogInRequest logInRequest) {
    logger.info("Inside authenticate()");
    try {
      logger.info("Authentication Request payload: {}", logInRequest);
      authManager.authenticate(new UsernamePasswordAuthenticationToken(logInRequest.getEmail(), logInRequest.getPassword()));
    } catch (Exception e) {
      e.getStackTrace();
    }
    logger.info("retrieving  user info after extracting mail id from request");
    User user = userRepository.findByEmail(logInRequest.getEmail()).orElseThrow();

    logger.info("generating access token");
    String accessToken = jwtService.generateToken(user);
    logger.info("generating refresh token");
    String refreshToken = jwtService.generateRefreshToken(user);

    /**
     * System.out.println("From AuthService  authenticate() ------");
     System.out.println("accessToken: " + accessToken);
     System.out.println("refreshToken: " + refreshToken);
     */
    logger.info("revoking previous tokens");
    revokeAllUserTokens(user);

    logger.info("saving the new access & refresh tokens to db");
//        first we revoke the existing tokens of a user then assign a new one which has not been revoked.
    Token savedToken = saveUserToken(user, accessToken, refreshToken);

    LogInResponse logInResponse = LogInResponse.builder().username(logInRequest.getEmail()).accessToken(accessToken).refreshToken(refreshToken).accessExpiresIn(savedToken.getExpiresIn()).build();
    logger.info("Returning authentication response {}", logInResponse);
    return logInResponse;
  }

  private Token saveUserToken(User user, String accessToken, String refreshToken) {
    logger.info("Saving access & refresh token for user: {}", user);
    Token token = Token.builder().user(user).accessToken(accessToken).refreshToken(refreshToken).tokenType(TokenType.BEARER).isRevoked(false).isExpired(false).expiresIn(UAuthConstants.TOKEN_VALIDITY_IN_SEC).build();
    return tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {

    List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());

    if (validUserTokens.isEmpty()) {
      logger.info("No prior access & refresh token exists for user: {}", user);
      return;
    }

    logger.info("Revoking all access & refresh token for user: {}", user);
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      logger.info("Auth token not found in the HTTP Header");
      return;
    }

    refreshToken = authHeader.substring(7);
    logger.info("extracting username from refresh-token");
//      extract the userEmail from JWT Token
    userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {

      User user = this.userRepository.findByEmail(userEmail).orElseThrow();

      /**
       * cross-check the validity of refreshToken from DB
       Boolean isTokenValid = tokenRepository.findByRefreshToken(refreshToken)
       .map(token -> !token.isExpired() && !token.isRevoked())
       .orElse(false);
       */

      logger.info("checking the validity of refresh-token");
      if (jwtService.isTokenValid(refreshToken, user)) {
        String accessToken = jwtService.generateToken(user);
        LogInResponse authResponse = LogInResponse.builder().username(userEmail).accessExpiresIn(UAuthConstants.TOKEN_VALIDITY_IN_SEC).accessToken(accessToken).refreshToken(refreshToken).build();
        logger.info("Revoking all prior access tokens before assigning new ones");
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken, refreshToken);
        logger.info("writing authentication response to HttpServletResponse Object");
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
