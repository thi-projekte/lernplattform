import axios from 'axios';
import keycloak from '../keycloak.ts';


export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_MYND_BACKEND_URI,
  timeout: 5000,
  headers: {
    'Accept': 'application/json'
  }
});

apiClient.interceptors.request.use(
  (config) => {
    config.headers.Authorization = "Bearer " + keycloak.token;

    return config;
  }
)