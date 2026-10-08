package org.dev.eclipse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dev.eclipse.config.AdminProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminIpRestrictionFilter extends OncePerRequestFilter {

    private final boolean ipRestrictionEnabled;
    private final Set<String> allowedIps;
    private final Set<String> trustedProxies;

    public AdminIpRestrictionFilter(AdminProperties adminProperties) {
        this.ipRestrictionEnabled = adminProperties.isIpRestrictionEnabled();
        this.allowedIps = adminProperties.getAllowedIps().stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        this.trustedProxies = adminProperties.getTrustedProxies().stream()
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !ipRestrictionEnabled || !path.startsWith("/api/v1/admin");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = extractClientIp(request);

        if (clientIp == null || !allowedIps.contains(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized ip address: " + clientIp + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (trustedProxies.contains(remoteAddr)) {
            String header = request.getHeader("X-Forwarded-For");
            if (header != null && !header.isEmpty()) {
                return header.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }
}
