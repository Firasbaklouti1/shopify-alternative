package com.firas.saas.app.security;

import com.firas.saas.app.entity.AppAccessToken;
import com.firas.saas.app.entity.InstallationStatus;
import com.firas.saas.app.repository.AppAccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Filter to authenticate app API requests using access tokens.
 * This filter runs for /api/v1/app/** endpoints.
 *
 * Apps send their access token in the Authorization header:
 * Authorization: Bearer <access_token>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppTokenAuthFilter extends OncePerRequestFilter {

    private final AppAccessTokenRepository tokenRepository;

    private static final String APP_API_PATH = "/api/v1/app/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Only process app API requests
        if (!requestPath.startsWith(APP_API_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                Optional<AppAccessToken> accessTokenOpt = tokenRepository.findByTokenValue(token);

                if (accessTokenOpt.isPresent()) {
                    AppAccessToken accessToken = accessTokenOpt.get();

                    // Validate token
                    if (validateToken(accessToken)) {
                        AppPrincipal principal = new AppPrincipal(accessToken);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated app {} for tenant {}", principal.getClientId(), principal.getTenantId());
                    } else {
                        log.debug("Invalid or expired token for request: {}", requestPath);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Token is invalid, expired, or revoked\"}");
                        return;
                    }
                } else {
                    log.debug("Token not found: {}", requestPath);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid access token\"}");
                    return;
                }
            } else {
                log.debug("No token provided for app API request: {}", requestPath);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access token required\"}");
                return;
            }
        } catch (Exception e) {
            log.error("Error authenticating app token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication error\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(AppAccessToken token) {
        // Check if token is revoked
        if (token.isRevoked()) {
            log.debug("Token is revoked");
            return false;
        }

        // Check if token is expired
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            log.debug("Token is expired");
            return false;
        }

        // Check if installation is still active
        if (token.getInstallation().getStatus() != InstallationStatus.ACTIVE) {
            log.debug("Installation is not active");
            return false;
        }

        return true;
    }
}
