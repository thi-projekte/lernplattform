import axios from 'axios';
import keycloak from '../keycloak.ts';
import type { ZodType } from 'zod';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_MYND_BACKEND_URI,
  timeout: 5000,
  headers: {
    Accept: 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  if (keycloak.isTokenExpired()) {
    await keycloak.updateToken(300);
  }

  config.headers.Authorization = 'Bearer ' + keycloak.token;

  return config;
});

export const safeValidateApiResponseContent = <T>(schema: ZodType, data: T) => {
  const validationResult = schema.safeParse(data);
  if (validationResult.error) {
    console.error(validationResult.error);
  }

  return data;
};
