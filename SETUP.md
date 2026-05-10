# Setup instructions

Setting up the MYnd project requires some precise and specfic configurations all documented in this file.

First you have to setup the basic dev environment from [Winfprojekt](https://winfprojekt.de/docs/technik/setup).

### 1. Setup repo

```bash
git clone https://github.com/thi-projekte/lernplattform.git
```

### 2. Docker setup

```bash
docker compose -f docker-compose.dev.yml up -d
```

Now the containers should be running. To ensure run this command:

```bash
docker compose -f docker-compose.dev.yml ps
```

The services 
- database
- keycloak
- maildev
- minio

should be running now.

### Keycloak setup

Open [http://localhost:8081](http://localhost:8081) and login with the credentials `admin`/`admin`.

In the following I will describe detailed what to select for the setup.

#### Setup registration 

- Click on "Realm settings"
- Click on the "Login" tab
- Enable "User Registration"
- Disable "Login with Email"
- Enable "Duplicate Emails"

#### Setup Frontend client

- Click on "Clients"
- Click on "Create Client"
- Client-ID: `mynd`
- Click "next"
- Click "next again"
- Valid redirect URIs: `*`
- Web Origins: `*`
- Click "save"
- Click on the "Roles" tab
- Click "Create Role"
- Role name: `builder`
- Click "save"
- Create another role: `learner`


#### Setup backend client

- Click on "Clients"
- Click on "Create Client"
- Client-ID: `mynd-backend`
- Click "next"
- Enable "Client Authentication"
- Disable "Standard Flow"
- Enable "Service Account Roles"
- Click "Next"
- Click "save"
- Click on "Service Account Roles" tab
- Click "Assign Role" - "Realm Role" - "admin" - "Assign"
- Click on "Credentials" tab
- Copy the "Client Secret"
- Set the copied value for the `KEYCLOAK_BACKEND_CLIENT_SECRET` environment variable in `mynd-backend/.env`

### Minio (S3) setup

- Open [http://localhost:9001](http://localhost:9001) and login with the credentials `admin`/`admin1234567`
- Click "Create Bucket"
- Bucket name: `default`
- Click "Create Bucket"

### Start backend initially

Open the `mynd-backend` directory in your IntelliJ Idea IDE. Wait a few seconds for the dependencies to be downloaded and the IDE to index everything.

Click on the "Run" button in the IDE to start the backend application.

Wait for the application to start and verify that it is running by opening [http://localhost:8080](http://localhost:8080) in your browser. You should see the Quarkus default page.

### Start the frontend

Open the `frontend` directory in your IDE.

Open the terminal an run those command:

```bash
npm install
```

To start the frontend application, run the following command in the terminal:

```bash
npm run dev
```

**NOTE:** If you want to develop the keycloak theme that is also part of the frontend you can use `npm run keycloak` to build the theme and `npx keycloakify start-keycloak`
