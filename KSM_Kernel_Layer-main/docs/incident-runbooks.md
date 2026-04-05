# Incident Runbooks

## 1. Saturation HTTP / 429

Symptomes:
- hausse des `429`
- hausse des compteurs quota/rate limit

Verification:
1. Nginx rate limiting par IP et par tenant
2. metrique `iwm_quotas_tenant_requests_rejected_total`
3. distribution des tenants affectes

Actions:
1. identifier si le tenant est legitime ou abusif
2. augmenter temporairement le quota si besoin metier reel
3. sinon maintenir le plafond et ouvrir un canal avec le client concerne

## 2. Backlog outbox

Symptomes:
- `iwm.outbox.pending` augmente
- `iwm.outbox.oldest_pending_age.seconds` depasse le seuil

Verification:
1. etat Kafka
2. etat du relay
3. volume de `DEAD_LETTER`

Actions:
1. verifier topic principal et dead-letter
2. augmenter temporairement `IWM_OUTBOX_RELAY_BATCH_SIZE`
3. augmenter `IWM_OUTBOX_RELAY_DELIVERY_CONCURRENCY` si Kafka suit
4. lancer le drill de replay si necessaire sur un environnement de test

## 3. Panne PostgreSQL

Symptomes:
- erreurs 5xx
- health DB rouge
- latence applicative forte

Actions:
1. confirmer la panne ou la saturation
2. verifier le pool R2DBC
3. reduire temporairement la pression si besoin via les limites gateway
4. restaurer PostgreSQL avant toute action sur les projections ou Kafka

## 4. Panne Kafka

Symptomes:
- relay outbox en echec
- `iwm.outbox.pending` monte

Actions:
1. verifier broker, topic et partitions
2. verifier `dead-letter`
3. ne pas purger l'outbox primaire
4. restaurer Kafka, puis laisser le relay drainer

## 5. Recherche Elasticsearch degradee

Symptomes:
- endpoints `search` lents ou indisponibles

Actions:
1. verifier exporter Elasticsearch et heap
2. verifier fraicheur des projections
3. rappeler que PostgreSQL reste la source de verite
4. isoler l'incident search sans toucher aux flux transactionnels
