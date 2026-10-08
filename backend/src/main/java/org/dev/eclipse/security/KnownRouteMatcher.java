package org.dev.eclipse.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class KnownRouteMatcher {

    private static final Set<String> FRAMEWORK_PATHS = Set.of(
            "/",
            "/error",
            "/favicon.ico",
            "/eclipse.svg",
            "/swagger-ui.html"
    );

    private static final List<String> FRAMEWORK_PREFIXES = List.of(
            "/ws/",
            "/error/",
            "/uploads/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<PathPattern> mappedPatterns;

    public KnownRouteMatcher(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @PostConstruct
    void initialize() {
        this.mappedPatterns = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(this::extractPatternValues)
                .distinct()
                .map(PathPatternParserHolder::parse)
                .toList();
    }

    public boolean matches(String requestPath) {
        if (matchesFrameworkPath(requestPath)) {
            return true;
        }

        PathContainer pathContainer = PathContainer.parsePath(requestPath);
        return mappedPatterns.stream().anyMatch(pattern -> pattern.matches(pathContainer));
    }

    private boolean matchesFrameworkPath(String requestPath) {
        if (FRAMEWORK_PATHS.contains(requestPath)) {
            return true;
        }

        return FRAMEWORK_PREFIXES.stream().anyMatch(requestPath::startsWith);
    }

    private Stream<String> extractPatternValues(RequestMappingInfo mappingInfo) {
        PathPatternsRequestCondition pathPatternsCondition = mappingInfo.getPathPatternsCondition();
        if (pathPatternsCondition == null) {
            return Stream.empty();
        }

        return pathPatternsCondition.getPatterns().stream()
                .map(PathPattern::getPatternString);
    }

    private static final class PathPatternParserHolder {
        private static PathPattern parse(String pattern) {
            return PathPatternParserHolderInstance.PARSER.parse(pattern);
        }
    }

    private static final class PathPatternParserHolderInstance {
        private static final PathPatternParser PARSER =
                new PathPatternParser();
    }
}
