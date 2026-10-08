package org.dev.eclipse.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class EndpointExistenceFilter extends OncePerRequestFilter {

    private final KnownRouteMatcher knownRouteMatcher;

    public EndpointExistenceFilter(KnownRouteMatcher knownRouteMatcher) {
        this.knownRouteMatcher = knownRouteMatcher;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        DispatcherType dispatcherType = request.getDispatcherType();
        return dispatcherType == DispatcherType.ERROR || dispatcherType == DispatcherType.ASYNC;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (knownRouteMatcher.matches(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
