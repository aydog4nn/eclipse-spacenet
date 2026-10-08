package org.dev.eclipse.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "audit_logs")
public class AuditLogDocument extends ElasticBaseDocument {

    @Id
    private String id;

    private String username;
    private String action;
    private String resource;
    private String details;
    private String ipAddress;

    public AuditLogDocument() {
        super();
    }

    public AuditLogDocument(String username, String action, String resource, String details, String ipAddress) {
        super();
        this.username = username;
        this.action = action;
        this.resource = resource;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
