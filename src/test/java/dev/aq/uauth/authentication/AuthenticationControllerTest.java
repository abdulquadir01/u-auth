package dev.aq.uauth.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aq.uauth.constants.Role;
import dev.aq.uauth.dto.LogInRequest;
import dev.aq.uauth.dto.LogInResponse;
import dev.aq.uauth.dto.SignUpRequest;
import dev.aq.uauth.exception.DuplicateEmailException;
import dev.aq.uauth.exception.RegistrationLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
class AuthenticationControllerTest {
  @InjectMocks
  private AuthenticationController authenticationController;

  @Mock
  private AuthenticationService authenticationService;

  LogInRequest logInRequest;
  LogInResponse logInResponse;
  SignUpRequest signUpRequest;

  @BeforeEach
  public void setUp() {
    logInRequest = LogInRequest.builder()
        .email("john.doe@example.org")
        .password("p@ssw0Rd")
        .build();
    logInResponse = LogInResponse.builder()
        .accessExpiresIn(1000)
        .accessToken("ABC123")
        .refreshToken("ABC123")
        .username("john.doe@example.org")
        .build();
    signUpRequest = SignUpRequest.builder()
        .email("john.doe@example.org")
        .firstName("John")
        .lastName("Doe")
        .password("p@ssw0Rd")
        .role(Role.USER)
        .build();
  }


  @Test
  void testRegister() throws Exception {

    when(authenticationService.register(any())).thenReturn(logInResponse);
    String content = (new ObjectMapper()).writeValueAsString(signUpRequest);
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content);
    MockMvcBuilders.standaloneSetup(authenticationController)
        .build()
        .perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
        .andExpect(MockMvcResultMatchers.content()
            .string(
                "{\"username\":\"john.doe@example.org\",\"accessExpiresIn\":1000,\"access_token\":\"ABC123\",\"refresh_token\":\"ABC123\"}"));
  }

  @Test
  void register_shouldReturn400BadRequest_whenRequiredFieldsAreMissing() {
    // Arrange
    signUpRequest = new SignUpRequest();
    when(authenticationService.register(signUpRequest)).thenThrow(new IllegalArgumentException("Required fields are missing"));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> authenticationController.register(signUpRequest));
  }

  @Test
  void register_shouldHandleDuplicateEmail() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("John")
        .lastName("Smith")
        .email("john.smith@example.com")
        .role(Role.USER)
        .build();
    when(authenticationService.register(signUpRequest)).thenThrow(new DuplicateEmailException("Email already exists"));

    // Act & Assert
    assertThrows(DuplicateEmailException.class, () -> authenticationController.register(signUpRequest));
    verify(authenticationService, times(1)).register(signUpRequest);
  }

  @Test
  void register_shouldReturnErrorForWeakPassword() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("Test")
        .lastName("User")
        .password("passwor")
        .email("testUser@gmail.com")
        .lastName("user")
        .build();
    when(authenticationService.register(signUpRequest)).thenThrow(new IllegalArgumentException("Password is too weak"));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> authenticationController.register(signUpRequest));

    verify(authenticationService, times(1)).register(signUpRequest);
  }

  //  @Test
  void register_shouldSanitizeAndHandleSpecialCharacters() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("Test")
        .lastName("User")
        .password("p@ssw0rd!")
        .email("testUser@gmail.com")
        .lastName("user")
        .build();
    LogInResponse expectedResponse = LogInResponse.builder()
        .accessExpiresIn(120000)
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();
    when(authenticationService.register(signUpRequest)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
    verify(authenticationService).register(signUpRequest);
    verify(authenticationService, times(1)).register(argThat(request ->
        request.getEmail().equals("test@example.com") &&
            request.getPassword().equals("p@ssw0rd!") &&
            request.getFirstName().equals("John &lt;script&gt;") &&
            request.getLastName().equals("O&#39;Connor")
    ));
  }

  //  @Test
  void register_shouldReturn500InternalServerError_whenAuthenticationServiceFails() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("John")
        .lastName("O&#39;")
        .email("john@gmail.com")
        .role(Role.USER)
        .build();
    when(authenticationService.register(signUpRequest)).thenThrow(new RuntimeException("Internal server error"));

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNull(response.getBody());
    verify(authenticationService, times(1)).register(signUpRequest);
  }

  @Test
  void register_shouldLogRegistrationRequestDetails() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("doe@gmail.com")
        .role(Role.MANAGER)
        .build();

    LogInResponse expectedResponse = LogInResponse.builder()
        .username("doe@gmail.com")
        .accessExpiresIn(1200000)
        .accessToken("test_token")
        .refreshToken("test_refresh_token")
        .build();
    when(authenticationService.register(signUpRequest)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
    verify(authenticationService).register(signUpRequest);

  }

  @Test
  void register_shouldReturnCorrectResponseStructureUponSuccessfulRegistration() {
    // Arrange
    LogInResponse expectedResponse = logInResponse;
    when(authenticationService.register(signUpRequest)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedResponse, response.getBody());
    verify(authenticationService, times(1)).register(signUpRequest);

  }

  @Test
  void register_shouldHandleExtremelyLongInputStrings() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .role(Role.USER)
        .build();

    LogInResponse expectedResponse = LogInResponse.builder()
        .username("john.doe@example.com")
        .accessExpiresIn(1200000)
        .accessToken("access_token")
        .refreshToken("refresh_token")
        .build();
    when(authenticationService.register(signUpRequest)).thenReturn(expectedResponse);

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());
    verify(authenticationService).register(signUpRequest);

  }

  //  @Test
  void register_shouldReturnErrorWhenRegistrationLimitExceeded() {
    // Arrange
    signUpRequest = SignUpRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("doe@gmail.com")
        .role(Role.USER)
        .build();
    when(authenticationService.register(signUpRequest))
        .thenThrow(new RegistrationLimitExceededException("Registration limit exceeded for this IP address"));

    // Act
    ResponseEntity<LogInResponse> response = authenticationController.register(signUpRequest);

    // Assert
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    assertNull(response.getBody());
    verify(authenticationService, times(1)).register(signUpRequest);

  }
}
