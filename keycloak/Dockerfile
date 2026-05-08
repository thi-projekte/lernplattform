FROM node:22-slim AS build-stage

WORKDIR /app

COPY frontend/package.json ./
RUN npm install --platform=linux --arch=x64

COPY ./frontend .
RUN npm run keycloak

FROM quay.io/keycloak/keycloak:26.6 AS final

COPY --from=build-stage /app/dist_keycloak/keycloak-theme-for-kc-all-other-versions.jar /opt/keycloak/providers/theme.jar