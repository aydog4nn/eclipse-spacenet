package org.dev.eclipse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    private boolean ipRestrictionEnabled = false;
    private List<String> allowedIps = new ArrayList<>();
    private List<String> trustedProxies = new ArrayList<>();

    public boolean isIpRestrictionEnabled() {
        return ipRestrictionEnabled;
    }

    public void setIpRestrictionEnabled(boolean ipRestrictionEnabled) {
        this.ipRestrictionEnabled = ipRestrictionEnabled;
    }

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(List<String> allowedIps) {
        this.allowedIps = allowedIps;
    }

    public List<String> getTrustedProxies() {
        return trustedProxies;
    }

    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies;
    }
}
