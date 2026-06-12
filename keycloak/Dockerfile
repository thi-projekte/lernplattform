FROM node:22-slim AS build-stage

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME (helpful for many Node wrappers that call Java)
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="${PATH}:${MAVEN_HOME}/bin"

RUN java -version && mvn -version

WORKDIR /app

COPY frontend/package.json ./
RUN npm install --platform=linux --arch=x64 --legacy-peer-deps

COPY ./frontend .

RUN npm run keycloak

FROM quay.io/keycloak/keycloak:latest AS keycloak-builder

COPY --from=build-stage /app/dist_keycloak/keycloak-theme-for-kc-all-other-versions.jar /opt/keycloak/providers/theme.jar

ENV KC_HEALTH_ENABLED=true
ENV KC_DB=postgres

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:latest

COPY --from=keycloak-builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]