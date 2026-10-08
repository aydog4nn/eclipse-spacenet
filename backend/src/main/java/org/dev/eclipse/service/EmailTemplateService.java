package org.dev.eclipse.service;

import java.util.Map;

public interface EmailTemplateService {

    String build(String templatePath, Map<String, Object> variables);
}
