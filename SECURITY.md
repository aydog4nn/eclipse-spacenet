# Security and public release

## Cleanup performed

- Removed embedded Gemini key, SMTP application password, JWT signing secret, database password and bootstrap admin password.
- Removed personal email defaults, original host IP addresses and local developer paths from published configuration and documentation.
- Replaced secrets with environment variables; bootstrap admin creation is off by default.
- Restricted HTTP and WebSocket origins to the configured frontend origin.
- Distinguished access, refresh and CSRF JWTs; API access requires an active account and refresh rejects blacklisted tokens.
- Restricted STOMP subscriptions to authorized projects and blocked client sends to broker destinations.
- Required authentication for uploaded files, rejected symlink escapes and used attachment/private response headers.
- Removed internal exception details from generic HTTP and chatbot errors.
- Bound local infrastructure to loopback and excluded local secrets, build outputs and runtime data from source control.

## Source-only portfolio scope

This hackathon project is shared for source-code review, with no live deployment planned. All original credential values were removed. Environment examples contain empty secret fields, not usable keys. Integration code remains available as part of the portfolio example; local execution would need separately supplied configuration.

The supplied directories have no `.git` history. Cleanup covers the supplied source and the generated archive; past remote commits, forks and other copies are outside this package. Removing values from source does not change provider accounts.

## Dependency validation

Frontend dependencies were updated with compatible security fixes; npm audit returned zero findings on 2026-10-08. The backend was migrated to Spring Boot 4.1.1, Springdoc 3.1.1 and Jackson 3 following the official migration guide, with explicit Tomcat 11.0.25 and Jackson 3.1.7 / 2.21.7 security patches. Elasticsearch local service follows the managed client version. Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

The final OSV query returned no findings for 145 packaged Maven artifacts on 2026-10-08. This describes the queried database and versions at that time, not a guarantee against undiscovered issues.

Compilation and six targeted security regression tests passed. The original full-context test was not run without local credentials and infrastructure.

## Check limits

Pattern scans and targeted tests cover the supplied source and queried dependencies. They do not prove the absence of every possible vulnerability. Full live integration flows were not tested and are outside this source-only portfolio release.

Local environment files, uploads, logs, database backups and generated build bundles are excluded from the sharing archive.
