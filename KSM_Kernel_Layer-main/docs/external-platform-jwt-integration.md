# Integration JWT Externe

Ce document decrit comment une autre plateforme consomme l'auth du kernel.

## 1. Login

Appel:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: <client-id>" \
  -H "X-Api-Key: <client-secret>" \
  -H "X-Tenant-Id: <tenant-id>" \
  -d '{
    "principal": "user-or-email",
    "password": "secret"
  }'
```

Reponse utile:

```json
{
  "data": {
    "id": "user-id",
    "tenantId": "tenant-id",
    "actorId": "actor-id",
    "accessToken": "<jwt-rs256>",
    "sessionToken": "<jwt-rs256>",
    "tokenType": "Bearer",
    "expiresInSeconds": 900,
    "authorities": ["organizations:write"],
    "organizations": [
      {
        "organizationId": "uuid",
        "organizationCode": "ORG-001",
        "displayName": "Display Name",
        "legalName": "Legal Name",
        "services": ["COMMERCIAL", "PRODUCT", "INVENTORY"]
      }
    ]
  }
}
```

`accessToken` est la valeur de reference.
`sessionToken` reste un alias de compatibilite.

## 2. Appeler une API protegee

```bash
curl http://localhost:8080/api/users/me \
  -H "X-Client-Id: <client-id>" \
  -H "X-Api-Key: <api-key>" \
  -H "X-Tenant-Id: <tenant-id>" \
  -H "Authorization: Bearer <access-token>"
```

Si l'appel est scope organisation ou agence:

- `X-Organization-Id: <organization-id>`
- `X-Agency-Id: <agency-id>`

Pour les endpoints metier scopes organisation:
- `X-Organization-Id` est obligatoire
- l'organisation doit etre abonnee au service requis
- sinon le kernel retourne:
  - `400 ORGANIZATION_CONTEXT_REQUIRED`
  - ou `403 ORGANIZATION_SERVICE_NOT_SUBSCRIBED`

## 3. Verifier le JWT cote plateforme consommatrice

Recuperer le JWK Set public:

```bash
curl http://localhost:8080/.well-known/jwks.json
```

Le token est signe en `RS256`.

Claims utiles:

- `sub` : `userId`
- `tid` : `tenantId`
- `oid` : `organizationId` si present
- `aid` : `agencyId` si present
- `actor` : `actorId` si present
- `permissions` : liste de permissions
- `iss` : issuer configure
- `exp` : expiration
- `jti` : identifiant unique du token

## 4. Regles d'integration

- toujours envoyer `X-Tenant-Id`
- toujours envoyer `X-Client-Id`
- toujours envoyer `X-Api-Key`
- utiliser `Authorization: Bearer <accessToken>`
- utiliser `organizations[].services` pour decider quels modules l'organisation consomme
- verifier `iss`, `exp`, `aud` et la signature `RS256`
- utiliser `/.well-known/jwks.json` pour la verification distante

## 5. Cas multi-plateformes

Une autre plateforme peut:

- deleguer le login au kernel
- stocker le `Bearer accessToken`
- verifier localement le token avec le JWKS
- utiliser les claims `tid`, `oid`, `aid`, `permissions` pour piloter son acces
