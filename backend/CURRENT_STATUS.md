# Backend status

See the repository README for implemented modules, local setup and current technology versions. Security cleanup and release limitations are recorded in `../SECURITY.md`.

The supplied source includes no Flyway migrations. Development creates/updates schema through Hibernate; production uses schema validation and requires separately provisioned schema. Full integration tests require PostgreSQL, Redis, Elasticsearch and local environment configuration.
