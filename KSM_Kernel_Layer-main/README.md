# IWM Backend

Backend Spring Boot multi-modules aligne sur conceptioKernel_V3.

## Modules
- iwm-kernel-core
- iwm-common-core
- iwm-actor-core
- iwm-organization-core
- iwm-tp-core
- iwm-auth-core
- iwm-roles-core
- iwm-administration-core
- iwm-file-core
- iwm-product-core
- iwm-inventory-core
- iwm-resource-core
- iwm-settings-core
- iwm-sales-core
- iwm-accounting-core
- iwm-treasury-core
- iwm-bootstrap

## Principes
- monolithe modulaire
- architecture hexagonale par module
- dependances inter-modules limitees aux contrats V3
- iwm-bootstrap est le seul module executable
- migrations de base gerees par `Liquibase`
- outbox transactionnelle persistée dans PostgreSQL
- relay d outbox externe par `Kafka`
- consommateurs internes persistés pour les projections metier
- cache Redis des permissions utilisateur disponible via feature flag
- projections Elasticsearch de recherche disponibles via feature flag
- confirmation de commande branchee au stock disponible avec sorties de stock tracees par document source
- cycle complet de reglement de facture entre `accounting` et `treasury`
- cycle de vie ressource enrichi: reservation, affectation, desaffectation, reforme
- policies metier explicites pour les cas d usage sensibles
- administration generale avec catalogue de permissions, gestion des roles, clonage, assignations, options generales, options plateforme et audit admin
- RBAC multi-scope avec roles `SYSTEM`, `TENANT`, `ORGANIZATION`, `AGENCY`
- templates de roles par defaut: `GENERAL_ADMIN`, `ORGANIZATION_ADMIN`, `AGENCY_ADMIN`, `SALES_MANAGER`, `INVENTORY_MANAGER`, `ACCOUNTANT`, `TREASURY_OFFICER`, `RESOURCE_MANAGER`
- gouvernance admin de `BusinessActor`, `Organization` et `Agency`
- les options plateforme pilotent aussi les flux de self-service `BusinessActor`, `Organization` et `Agency`
- `tp-core` couvre maintenant clients, fournisseurs, prospects, agents commerciaux, conversion prospect -> client, comptes bancaires tiers, compte comptable tiers et statistiques commerciales
- `tp-core` couvre aussi la qualification commerciale au-dela du legacy: segment et score de qualification
- `auth-core` expose aussi le self-service utilisateur `users/me`, plan et onboarding
- `auth-core` emet maintenant un bearer access token JWT signe en `RS256`, avec alias `sessionToken` conservé pour compatibilite
- `kernel-core` authentifie maintenant les backends consommateurs via de vraies `ClientApplication` (`X-Client-Id` + `X-Api-Key`)
- `organization-core` gere maintenant les abonnements de services par organisation et expose `effectiveServices`
- `auth-core` retourne aussi les organisations accessibles et leurs services dans `login` et `users/me`
- `kernel-core` bloque maintenant les endpoints metier scopes organisation si `X-Organization-Id` est absent ou si l'organisation n'est pas abonnée au service requis
- `kernel-core` expose un audit systeme consultable
- `kernel-core` expose aussi le JWK Set public `/.well-known/jwks.json`
- `file-core` gere l upload/download de fichiers avec metadonnees en base, binaire sur stockage local configurable, validation de type MIME et durcissement anti path traversal

## Runtime par defaut
- profil runtime: `r2dbc`
- base de donnees: PostgreSQL
- migrations: `Liquibase`
- transport d evenements externe: `Kafka`
- topic canonique par defaut: `iwm.events.business`
- dead-letter topic par defaut: `iwm.events.dead-letter`
- consumers internes alimentant `integration.domain_event_projection`
- Redis: optionnel, active pour le cache des permissions
- Elasticsearch: optionnel, active pour les projections de recherche
- le mode server-to-server repose sur une `ClientApplication` bootstrap optionnelle configuree par environnement
- l auth utilisateur accepte un bearer JWT RS256 via `Authorization: Bearer ...`
- le mode dev/test peut auto-generer une paire RSA avec `IWM_JWT_AUTO_GENERATE_KEY_PAIR=true`

## Validation
- suite standard:
```bash
mvn -q test
```
- contrats PostgreSQL par module:
```bash
mvn -q -pl iwm-bootstrap -am \
  -Diwm.tests.r2dbc.enabled=true \
  -Dtest=IdentityAccessContractTests,OrganizationCatalogContractTests,SalesInventoryContractTests,AccountingTreasuryContractTests,ResourceContractTests \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
- integration Kafka:
```bash
mvn -q -Dtest=KafkaOutboxIntegrationTests \
  -Diwm.tests.kafka.enabled=true \
  -pl iwm-bootstrap -am \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
- garde-fous hexagonaux:
```bash
mvn -q -pl iwm-bootstrap -am \
  -Dtest=SearchAdapterArchitectureTests,IntegrationAdapterArchitectureTests,HexagonalModuleArchitectureTests \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

## Jeux de donnees de contrat
- chaque suite de contrat utilise un changelog Liquibase dedie
- les datasets sont reseedes a chaque execution via `runAlways`
- le nettoyage est centralise dans `db/changelog/contracts/sql/contract-cleanup.sql`
- les tests de contrat ne reposent pas sur `drop-first`; ils rejouent un etat deterministe

## Documentation
- CI: [docs/ci.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/ci.md)
- Redis et Elasticsearch: [docs/cache-search-strategy.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/cache-search-strategy.md)
- Infrastructure locale: [docs/local-infrastructure.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/local-infrastructure.md)
- Observabilite et exploitation: [docs/operations-readiness.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/operations-readiness.md)
- Rotation JWT/JWKS: [docs/yowAuth0_docs/jwt-key-rotation.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/yowAuth0_docs/jwt-key-rotation.md)
- Integration plateforme externe JWT: [docs/external-platform-jwt-integration.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/external-platform-jwt-integration.md)
- Client applications: [docs/client-applications-design.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/client-applications-design.md)
- Services d'organisation: [docs/organization-service-subscriptions.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/organization-service-subscriptions.md)
- Checklist pre-prod -> prod: [docs/preprod-prod-checklist.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/preprod-prod-checklist.md)
- Audit production readiness: [docs/production-readiness-audit.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/production-readiness-audit.md)
- Gel du socle: [docs/socle-freeze.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/socle-freeze.md)
- Gel de parite legacy: [docs/legacy-parity-freeze.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/legacy-parity-freeze.md)
- Roadmap post-legacy: [docs/post-ksm-v0-1-roadmap.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/post-ksm-v0-1-roadmap.md)
- Hardening: [docs/hardening-roadmap.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/hardening-roadmap.md)
- Scaling: [docs/scaling-strategy.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/scaling-strategy.md)
- Enrichissement metier: [docs/domain-enrichment-roadmap.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/domain-enrichment-roadmap.md)
- Industrialisation Docker: [docs/docker-industrialization.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/docker-industrialization.md)
- Conception `administration-core`: [docs/administration-core-design.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/administration-core-design.md)
- Gouvernance plateforme: [docs/platform-governance-design.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/platform-governance-design.md)
- Runtime deploiement: [docs/deployment-runtime.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/deployment-runtime.md)
- Performance et charge: [docs/performance-and-load.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/performance-and-load.md)
- Capacity planning: [docs/capacity-planning.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/capacity-planning.md)
- Resilience drills: [docs/resilience-drills.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/resilience-drills.md)
- Incident runbooks: [docs/incident-runbooks.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/incident-runbooks.md)
- Dashboards Grafana: [ops/grafana/README.md](/home/blhack/Projets/Kernel-core/iwm-backend/ops/grafana/README.md)
- Conception admin: [docs/administration-core-design.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/administration-core-design.md)
- Gouvernance plateforme: [docs/platform-governance-design.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/platform-governance-design.md)
- Release Docker:
  - build image: [scripts/build-release-image.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/build-release-image.sh)
  - verifier compose: [scripts/verify-docker-release.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/verify-docker-release.sh)

## Parite legacy gelee
- reference: [docs/ksm-v0-1-parity-matrix.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/ksm-v0-1-parity-matrix.md)
- gel de non-regression: [docs/legacy-parity-freeze.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/legacy-parity-freeze.md)
- execution locale:
```bash
./scripts/run-legacy-parity.sh memory
./scripts/run-legacy-parity.sh r2dbc
```

## Activation Redis et Elasticsearch
- Redis permissions:
  - `IWM_REDIS_PERMISSION_CACHE_ENABLED=true`
  - `IWM_REDIS_HOST`
  - `IWM_REDIS_PORT`
  - `IWM_REDIS_PASSWORD`
- Elasticsearch search:
  - `IWM_ELASTICSEARCH_SEARCH_ENABLED=true`
  - `IWM_ELASTICSEARCH_URIS`

## Recherche Elasticsearch exposee
- `GET /api/organizations/search?q=...&organizationType=...`
- `GET /api/products/search?organizationId=...&q=...&familyCode=...&status=...`
- `GET /api/third-parties/search?organizationId=...&q=...&role=...&prospect=...`
- `GET /api/resources/search?organizationId=...&q=...&agencyId=...&category=...&status=...`

## Note recherche reactive
- la couche application de recherche Elasticsearch est reactive et repose sur `ReactiveElasticsearchOperations`
- la presence de `elasticsearch-rest-client` dans les dependances est normale: c est le transport HTTP du client Java Elastic utilise par Spring Data
- le projet n utilise plus de contournement `WebClient` direct pour interroger Elasticsearch

## Gateway Nginx
- point d entree public: `gateway` Nginx
- l application n expose plus directement son port HTTP au host
- `X-User-Id` et `X-Actor-Id` entrants sont supprimes au gateway
- `/actuator/**` n est pas expose par le gateway
- `GET /healthz` est public via le gateway
- rate limiting Nginx par IP et par tenant
- quota backend par tenant via Redis, activable par configuration

## Surface ops
- `GET /actuator/health/operations`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`
- `GET /api/observability/runtime`
- `GET /actuator/**` hors `health`/`info` exige `X-Management-Api-Key`
- port management separable via `MANAGEMENT_SERVER_PORT`

## Infrastructure locale
- stack Docker locale: [docker-compose.infrastructure.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.infrastructure.yml)
- stack Docker complete avec application et gateway Nginx: [docker-compose.application.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.application.yml)
- configuration gateway Nginx: [ops/nginx/README.md](/home/blhack/Projets/Kernel-core/iwm-backend/ops/nginx/README.md)
- exemple d environnement local: [.env.local.example](/home/blhack/Projets/Kernel-core/iwm-backend/.env.local.example)
- exemple d environnement preprod: [.env.preprod.example](/home/blhack/Projets/Kernel-core/iwm-backend/.env.preprod.example)
- exemple d environnement prod: [.env.prod.example](/home/blhack/Projets/Kernel-core/iwm-backend/.env.prod.example)
- choix Kafka retenu localement:
  - `confluentinc/cp-zookeeper:7.6.1`
  - `confluentinc/cp-kafka:7.6.1`
- `bitnamilegacy/kafka:3.7.0` n est pas retenu dans ce projet pour eviter de multiplier les distributions Kafka sans gain technique clair

## Bootstrap securise
- `GET /api/client-applications`
- `POST /api/client-applications`
- `POST /api/client-applications/{clientApplicationId}/rotate-secret`
- `POST /api/client-applications/{clientApplicationId}/revoke`
- `POST /api/auth/register` et `POST /api/roles/**` ne sont plus ouverts a un simple client applicatif sans contexte utilisateur
- en exploitation normale, ces endpoints exigent un utilisateur authentifie porteur de `system:admin` ou `iam:admin`
- pour un bootstrap initial controle, on peut provisionner un `bootstrap client application` via les variables `IWM_BOOTSTRAP_CLIENT_*`
- ce client bootstrap doit rester sous secret manager et etre remplace progressivement par des client applications dediees

## Administration generale
- `GET /api/administration/permissions`
- `GET /api/administration/role-templates`
- `POST /api/administration/roles/defaults`
- `GET /api/administration/roles`
- `POST /api/administration/roles`
- `PATCH /api/administration/roles/{roleId}`
- `POST /api/administration/roles/{roleId}/clone`
- `PUT /api/administration/roles/{roleId}/permissions`
- `DELETE /api/administration/roles/{roleId}`
- `GET /api/administration/users/{userId}/roles`
- `POST /api/administration/users/{userId}/roles`
- `DELETE /api/administration/users/{userId}/roles/{assignmentId}`
- `GET /api/administration/settings/platform-options`
- `PUT /api/administration/settings/platform-options`
- `GET /api/administration/settings/general-options`
- `PUT /api/administration/settings/general-options`
- `GET /api/administration/audit`
- `GET /api/administration/governance/business-actors`
- `POST /api/administration/governance/business-actors/{businessActorId}`
- `GET /api/administration/governance/organizations`
- `POST /api/administration/governance/organizations/{organizationId}`
- `GET /api/administration/governance/agencies`
- `POST /api/administration/governance/agencies/{agencyId}`

## Surface tiers et self-service
- `GET/POST/PATCH/DELETE /api/clients`
- `GET/POST/PATCH/DELETE /api/suppliers`
- `GET/POST/PATCH/DELETE /api/prospects`
- `GET/POST/PATCH/DELETE /api/sales-agents`
- `GET /api/customers`
- `POST /api/prospects/{thirdPartyId}/convert-to-customer`
- `GET /api/{clients|suppliers|prospects|sales-agents}/statistics`
- `GET/POST/DELETE /api/{clients|suppliers|prospects|sales-agents}/{thirdPartyId}/bank-accounts`
- `POST /api/{clients|suppliers|prospects|sales-agents}/{thirdPartyId}/bank-accounts/{bankAccountId}/primary`
- `PATCH /api/{clients|suppliers|prospects|sales-agents}/{thirdPartyId}/accounting-account`
- `PATCH /api/{clients|suppliers|prospects|sales-agents}/{thirdPartyId}/qualification`
- `GET /api/third-parties/lookup/by-bank-account`
- `GET /api/third-parties/lookup/by-accounting-account`
- `GET /api/users/me`
- `PUT /api/users/me/plan`
- `PUT /api/users/me/onboarding`
- `GET /api/system-audits/me`
- `GET /api/system-audits/organization`
- `POST /api/files`
- `GET /api/files/{fileId}`

## Parametres fichiers
- `IWM_FILE_STORAGE_ROOT_PATH`
- `IWM_FILE_STORAGE_MAX_FILE_SIZE_BYTES`
- `IWM_FILE_STORAGE_ALLOWED_CONTENT_TYPES`
