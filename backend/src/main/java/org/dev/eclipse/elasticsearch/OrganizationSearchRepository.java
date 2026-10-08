package org.dev.eclipse.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrganizationSearchRepository extends ElasticsearchRepository<OrganizationSearchDocument, String> {
}
