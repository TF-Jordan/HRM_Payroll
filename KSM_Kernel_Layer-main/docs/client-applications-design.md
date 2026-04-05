# Client Applications

## Objet

Le kernel est appele par des backends consommateurs, pas par des frontends.

L'authentification serveur-vers-serveur repose donc sur une vraie notion de `ClientApplication` au lieu d'une cle globale partagee.

## Modele

Une `ClientApplication` represente un backend consommateur connu du kernel.

Champs principaux:
- `id`
- `clientId`
- `name`
- `description`
- `secretHash`
- `status`
- `systemManaged`
- `createdAt`
- `updatedAt`
- `lastAuthenticatedAt`
- `secretRotatedAt`

Statuts:
- `ACTIVE`
- `REVOKED`

## Contrat HTTP

Un appel backend -> kernel transporte:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- `Authorization: Bearer <jwt>` si un contexte utilisateur est porte
- `X-Organization-Id` si l'endpoint metier est scope organisation

Lecture:
- `X-Client-Id` identifie l'application appelante
- `X-Api-Key` est le secret de cette application
- `Authorization: Bearer ...` porte l'identite utilisateur
- `X-Tenant-Id` fixe le contexte tenant quand le JWT ne suffit pas ou quand le backend veut l'expliciter
- `X-Organization-Id` fixe le scope de securite organisationnel pour les services metier concernes

## Login

Le login utilisateur fait aussi partie du contrat inter-backends.

Donc `POST /api/auth/login` exige deja:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- body `principal/password`

La reponse de login retourne aussi `organizations[].services` pour que le backend consommateur sache quels modules exposer a l'utilisateur pour chaque organisation.

## Bootstrap

Pour un demarrage a froid, le kernel peut provisionner un client systeme gere par configuration:
- `IWM_BOOTSTRAP_CLIENT_ENABLED`
- `IWM_BOOTSTRAP_CLIENT_ID`
- `IWM_BOOTSTRAP_CLIENT_NAME`
- `IWM_BOOTSTRAP_CLIENT_DESCRIPTION`
- `IWM_BOOTSTRAP_CLIENT_SECRET`

Ce client bootstrap:
- est persiste comme une vraie `ClientApplication`
- est marque `systemManaged=true`
- peut etre rehydrate/rotate au redemarrage si la configuration change

## Administration

Endpoints:
- `GET /api/client-applications`
- `POST /api/client-applications`
- `POST /api/client-applications/{clientApplicationId}/rotate-secret`
- `POST /api/client-applications/{clientApplicationId}/revoke`

Ces endpoints exigent un utilisateur authentifie porteur de:
- `system:admin`
- ou `iam:admin`

## Bonnes pratiques

- ne jamais partager un meme secret entre plusieurs backends
- donner un `clientId` stable par plateforme consommatrice
- utiliser la rotation reguliere via l'endpoint dedie
- reserver le client bootstrap aux phases de provisioning et aux environnements controles
- preferer ensuite des client applications metier explicites:
  - `sales-platform-backend`
  - `admin-platform-backend`
  - `erp-backend`
