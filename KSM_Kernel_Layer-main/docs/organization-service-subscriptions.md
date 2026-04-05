# Organization Service Subscriptions

## Objet

Chaque organisation porte maintenant une liste explicite de services plateforme actives.

Cette liste sert a trois choses:
- exposer au backend consommateur quels modules l'organisation peut utiliser
- enrichir `POST /api/auth/login` et `GET /api/users/me`
- bloquer les endpoints metier quand l'organisation n'est pas abonnée au service requis

## Catalogue

Services obligatoires:
- `ORGANIZATION`
- `SETTINGS`

Services gerables par abonnement:
- `COMMERCIAL`
- `PRODUCT`
- `INVENTORY`
- `SALES`
- `ACCOUNTING`
- `TREASURY`
- `RESOURCE`

## Stockage

Le stockage canonique se fait dans:
- `organization.organization_service_subscription`

Contrainte:
- unicite `(tenant_id, organization_id, service_code)`

## API organization-core

- `GET /api/organizations/services/catalog`
- `GET /api/organizations/{organizationId}/services`
- `POST /api/organizations/{organizationId}/services`
- `DELETE /api/organizations/{organizationId}/services/{serviceCode}`

Le retour expose:
- `subscribedServices`
- `effectiveServices`

`effectiveServices` = services obligatoires + services souscrits.

## Exposition auth-core

`POST /api/auth/login` et `GET /api/users/me` retournent maintenant:

```json
{
  "data": {
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

Cela permet a un backend consommateur:
- de savoir quelles organisations l'utilisateur peut operer
- de savoir quels modules il doit afficher ou masquer pour chaque organisation

## Enforcement runtime

Le kernel applique maintenant un filtre d'entitlement organisationnel sur les prefixes metier:

- `/api/clients`
- `/api/customers`
- `/api/suppliers`
- `/api/prospects`
- `/api/sales-agents`
- `/api/third-parties`
- `/api/products`
- `/api/inventory`
- `/api/inventories`
- `/api/sales`
- `/api/accounting`
- `/api/treasury`
- `/api/banking`
- `/api/resources`

Regles:
- `X-Organization-Id` devient obligatoire sur ces endpoints
- si l'organisation n'a pas le service requis, le kernel retourne:
  - `403`
  - `errorCode = ORGANIZATION_SERVICE_NOT_SUBSCRIBED`

## Provisioning par defaut

A la creation d'une organisation:
- les services obligatoires restent implicites
- les services abonnables sont provisionnes par defaut pour conserver la compatibilite des flux existants

La restriction se fait ensuite par desabonnement explicite si la plateforme veut fermer certains modules.

## Integration backend -> kernel

Pour un endpoint metier scope organisation, le backend consommateur doit envoyer:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- `Authorization: Bearer <jwt>`
- `X-Organization-Id`

Le body peut aussi contenir `organizationId`, mais ce n'est pas ce champ qui porte le scope de securite.
Le scope de securite est le header `X-Organization-Id`.
