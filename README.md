# MYnd Learning platform

Later we have to find a good catchy description ;-)

## Setup

To setup the database, process engine and keycloak you need to have docker installed.

For the dev environment simply run
```shell
docker compose -f docker-compose.dev.yml up -d
```

To finalize the keycloak setup, follow [this setup instructions](https://darkaico.medium.com/building-a-secure-authentication-system-with-keycloak-react-and-flask-35aeee04e37a).
Our client_id should be `mynd`.

Now you should be ready to start developing!

## Contributing

Take a look at the [Contribution guide](./CONTRIBUTING.md)