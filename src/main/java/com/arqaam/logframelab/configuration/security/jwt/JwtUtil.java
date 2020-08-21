package com.arqaam.logframelab.configuration.security.jwt;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public class JwtUtil {

  private JwtUtil() {
  }

  public static String getJwtFromRequest(HttpServletRequest request,
      JwtTokenProvider jwtTokenProvider) {
    String bearerToken = request.getHeader(jwtTokenProvider.getTokenHeader());
    if (StringUtils.isNotBlank(bearerToken) && bearerToken
        .startsWith(jwtTokenProvider.getTokenHeaderPrefix())) {
      return StringUtils.remove(bearerToken, jwtTokenProvider.getTokenHeaderPrefix());
    }

    return null;
  }
}
