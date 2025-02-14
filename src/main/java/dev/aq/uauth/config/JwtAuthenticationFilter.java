package dev.aq.uauth.config;

import dev.aq.uauth.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwtToken;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    jwtToken = authHeader.substring(7);
    // extract the userEmail from JWT Token
    userEmail = jwtService.extractUsername(jwtToken);

    /**
     * SecurityContextHolder.getContext().getAuthentication() == null
     * this means that the user is not authenticated yet.
     */
    if ( userEmail != null &&
      SecurityContextHolder.getContext().getAuthentication() == null
    ) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

      // cross-check the validity of token from DB
      Boolean isTokenValid = tokenRepository.findByAccessToken(jwtToken)
          .map(token -> !token.isExpired() && !token.isRevoked())
          .orElse(false);

      if (jwtService.isTokenValid(jwtToken, userDetails) && Boolean.TRUE.equals(isTokenValid)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}
