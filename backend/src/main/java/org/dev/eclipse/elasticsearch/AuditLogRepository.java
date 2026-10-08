package org.dev.eclipse.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuditLogRepository extends ElasticsearchRepository<AuditLogDocument, String> {
}
