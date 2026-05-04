import keycloak from './keycloak.ts';
import type { KeycloakResourceAccess } from 'keycloak-js';

export enum Role {
  Builder = 'builder'
}

export const isGranted = (roles: Role[]) => {
  const resourceAccess: KeycloakResourceAccess = keycloak.idTokenParsed?.resource_access as KeycloakResourceAccess;
  return roles.some((role) => resourceAccess['mynd'].roles.indexOf(role) > -1);
}