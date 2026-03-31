/* eslint-disable react-refresh/only-export-components */
import { createContext, type ReactNode, useContext } from 'react';
import type { KeycloakProfile } from 'keycloak-js';
import { useQuery } from '@tanstack/react-query';
import keycloak from '../keycloak.ts';

const UserContext = createContext<KeycloakProfile>({});

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const { data, isLoading } = useQuery({
    queryKey: ['keycloakProfile'],
    queryFn: () => keycloak.loadUserProfile(),
  });

  if (isLoading || !data) {
    return null;
  }

  return <UserContext value={data}>{children}</UserContext>;
};

export const useUserProfile = () => useContext(UserContext);
