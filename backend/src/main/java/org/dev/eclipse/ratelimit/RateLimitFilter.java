package org.dev.eclipse.ratelimit;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.ratelimit.paths.login}")
    private String loginPath;

    @Value("${app.ratelimit.paths.forgot-password}")
    private String forgotPasswordPath;

    @Value("${app.ratelimit.paths.reset-password}")
    private String resetPasswordPath;

    @Value("${app.ratelimit.paths.contact}")
    private String contactPath;

    @Value("${app.ratelimit.key-prefix}")
    private String keyPrefix;

    @Value("${app.ratelimit.reset-password.limit-for-period}")
    private int resetPasswordLimit;

    @Value("${app.ratelimit.reset-password.limit-refresh-period}")
    private Duration resetPasswordRefreshPeriod;

    @Value("${app.ratelimit.general.limit-for-period}")
    private int generalLimit;

    @Value("${app.ratelimit.general.limit-refresh-period}")
    private Duration generalRefreshPeriod;

    @Value("${app.ratelimit.contact.limit-for-period}")
    private int contactLimit;

    @Value("${app.ratelimit.contact.limit-refresh-period}")
    private Duration contactRefreshPeriod;

    public RateLimitFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        RateLimitPolicy policy = resolvePolicy(path);

        if (!allowRequest(request.getRemoteAddr(), path, policy)) {
            writeRateLimitResponse(response, resolveErrorMessage(path));
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        return !rateLimitedPaths().contains(request.getRequestURI());
    }

    private boolean allowRequest(String ipAddress, String path, RateLimitPolicy policy) {
        String redisKey = buildRedisKey(ipAddress, path);
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            return true;
        }

        if (currentCount == 1L) {
            redisTemplate.expire(redisKey, policy.refreshPeriod().toMillis(), TimeUnit.MILLISECONDS);
        }

        return currentCount <= policy.limitForPeriod();
    }

    private String buildRedisKey(String ipAddress, String path) {
        return keyPrefix + path + ":" + ipAddress;
    }

    private Set<String> rateLimitedPaths() {
        return Set.of(loginPath, forgotPasswordPath, resetPasswordPath, contactPath);
    }

    private RateLimitPolicy resolvePolicy(String path) {
        if (forgotPasswordPath.equalsIgnoreCase(path)) {
            return new RateLimitPolicy(resetPasswordLimit, resetPasswordRefreshPeriod);
        }

        if (contactPath.equalsIgnoreCase(path)) {
            return new RateLimitPolicy(contactLimit, contactRefreshPeriod);
        }

        return new RateLimitPolicy(generalLimit, generalRefreshPeriod);
    }

    private void writeRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        RestResponse<Object> errorResponse = RestResponse.fail(message);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String resolveErrorMessage(String path) {
        if (contactPath.equalsIgnoreCase(path)) {
            return "Kısa süre içinde çok fazla mesaj gönderdiniz. Lütfen daha sonra tekrar deneyiniz.";
        }

        return "Çok fazla istek yaptınız. Lütfen daha sonra tekrar deneyiniz.";
    }

    private record RateLimitPolicy(int limitForPeriod, Duration refreshPeriod) {
    }
}
