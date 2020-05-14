package com.arqaam.logframelab.configuration.security.jwt;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtTokenProvider jwtTokenProvider;

  private final UserDetailsService userDetailsService;

  @Value("${jwt.header}")
  private String tokenRequestHeader;

  @Value("${jwt.header.prefix}")
  private String tokenRequestHeaderPrefix;

  public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(httpServletRequest);

      if (StringUtils.isNotBlank(jwt) && jwtTokenProvider.isTokenValid(jwt)) {
        String username = jwtTokenProvider.getUsernameFromJws(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception ex) {
      logger.error("Failed to set user authentication in security context: ", ex);
      throw ex;
    }

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(tokenRequestHeader);
    if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(tokenRequestHeaderPrefix)) {
      return StringUtils.remove(bearerToken, tokenRequestHeaderPrefix);
    }

    return null;
  }
}
