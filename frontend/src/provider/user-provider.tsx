/* eslint-disable react-refresh/only-export-components */
import {createContext, type ReactNode, useCallback, useContext, useMemo} from 'react';
import type {KeycloakProfile, KeycloakTokenParsed} from 'keycloak-js';
import { useQuery } from '@tanstack/react-query';
import keycloak from '../keycloak.ts';

interface MyndUser {
  account: KeycloakProfile;
  idToken?: KeycloakTokenParsed;
  accessToken?: KeycloakTokenParsed;
}

export interface UserService {
  roles: string[];
  account: KeycloakProfile;
  isGranted: (role: string) => boolean;
}

const UserContext = createContext<MyndUser>({account: {}});

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const { data: account, isLoading: isProfileLoading } = useQuery({
    queryKey: ['keycloakProfile'],
    queryFn: () => keycloak.loadUserProfile(),
  });

  if ( isProfileLoading || !account) {
    return null;
  }

  return <UserContext value={{idToken: keycloak.idTokenParsed, accessToken: keycloak.tokenParsed, account}}>{children}</UserContext>;
};

export const useUserService = (): UserService => {

  const user = useContext(UserContext);

  const roles = useMemo<string[]>(()=> {

    const roleObjects = Object.values(user.accessToken?.resource_access ?? {});

    return [
      ...(user.accessToken?.realm_access?.roles ?? []),
      ...roleObjects.map((obj) => obj.roles).flat()
    ];
  }, [user]);

  const isGranted = useCallback((role: string): boolean => roles.indexOf(role) > -1, [roles]);

  return {account: user.account, roles, isGranted};

}
