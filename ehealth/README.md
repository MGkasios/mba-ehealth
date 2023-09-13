# How to run quarkus-dapr demo

## preparation

Before running this demo, please ensure that you have the following software installed:

- jdk 11: GraalVM
- maven 3.8.*
- quarkus 2.8.0
- docker
- dapr cli

### dapr install

```bash
dapr init
```

## Build Native Binary

```bash

# maven build
cd ehealth
mvn clean install

# build native binary for exams
cd ehealth
cd exams
quarkus build --native
# ensure exams binary file exists
ls -lh target/exams-1.0.5-SNAPSHOT-runner

# build native binary for permissions
cd ..
cd permissions
quarkus build --native
# ensure permissions binary file exists
ls -lh target/permissions-1.0.5-SNAPSHOT-runner
cd ..

build native binary for notifications
cd ..
cd notifications
quarkus build --native
# ensure notifications binary file exists
ls -lh target/notifications-1.0.5-SNAPSHOT-runner
cd ..

build native binary for notifications-email
cd ..
cd notifications-email
quarkus build --native
# ensure notifications-email binary file exists
ls -lh target/notifications-email-1.0.5-SNAPSHOT-runner
cd ..

build native binary for users
cd ..
cd users
quarkus build --native
# ensure users binary file exists
ls -lh target/permissions-1.0.5-SNAPSHOT-runner
cd ..
```

## Start Redis 

We will use redis as component for pubsub / state . 

if you just installed Dapr runtime by `dapr init` command, then redis should already run with docker:

```bash
# check if redis is running with docker
docker ps
# verify redis 6379 port
nc  -zv  127.0.0.1 6379
```

if redis is not running, please start it with docker command:

```bash
# start redis by docker
docker run -d -p 6379:6379 redis --requirepass ""
# verify that redis is listening on 6379 port
nc  -zv  127.0.0.1 6379
```

## Start Kong

```bash
# Build custom Kong image that includes the OIDC plugin
docker build -t kong:2.7.0-oidc ehealth/kong/

# Start Kong DB
docker-compose up -d kong-db

# Generate DB tables
docker-compose run --rm kong kong migrations bootstrap

# Start Kong
docker-compose up -d kong

# Setup APIs

# Exams
curl -s -X POST http://keycloak-host:8001/services -d name=exams-service -d url=http://ehealth-host:3500/v1.0/invoke/exams/method
#Use Id from response
curl -s -X POST http://keycloak-host:8001/routes -d service.id=fb9e8216-fef7-4ae4-ad43-a9a64508963c -d paths[]=/exam -d strip_path=false

# Permissions
curl -s -X POST http://keycloak-host:8001/services -d name=permissions-service -d url=http://ehealth-host:3502/v1.0/invoke/permissions/method
#Use Id from response
curl -s -X POST http://keycloak-host:8001/routes -d service.id=f953eb2d-7593-4304-8b08-4a927d5b2da6 -d paths[]=/permission -d strip_path=false

# Notifications
curl -s -X POST http://keycloak-host:8001/services -d name=notifications-service -d url=http://ehealth-host:3503/v1.0/invoke/notifications/method
#Use Id from response
curl -s -X POST http://keycloak-host:8001/routes -d service.id=c11abc36-a0e4-49a7-ba20-5f07da49532c -d paths[]=/notifications -d strip_path=false

# Users
curl -s -X POST http://keycloak-host:8001/services -d name=users-service -d url=http://ehealth-host:3505/v1.0/invoke/users/method
#Use Id from response
curl -s -X POST http://keycloak-host:8001/routes -d service.id=1b38c5b2-9fca-4ead-8a41-e004c60e903d -d paths[]=/user -d strip_path=false
```

## Start Keycloak

```bash
# Start Keycloak DB
docker-compose up -d keycloak-db

# Start Keycloak
docker-compose up -d keycloak
```
## Create Local Domain
Edit hosts file and add two new domains pointing in your ip
```bash
<ip> keycloak-host
<ip> ehealth-host
```

## Config Keycloak 
Open: http://keycloak-host:8180
Create a client and a user

```bash
HOST_IP=$(ipconfig getifaddr en0)
CLIENT_SECRET=<client_secret_from_keycloak>
curl -s -X POST http://keycloak-host:8001/plugins \
  -d name=oidc \
  -d config.client_id=kong \
  -d config.client_secret=${CLIENT_SECRET} \
  -d config.discovery=http://keycloak-host:8180/auth/realms/master/.well-known/openid-configuration \
  -d config.introspection_endpoint=http://keycloak-host:8180/auth/realms/master/protocol/openid-connect/token/introspect
  Result: {
    "created_at": 1542341927000,
    "config": {
        "response_type": "code",
        "realm": "kong",
    ...
}
```

if redis is not running, please start it with docker command:

```bash
# start redis by docker
docker run -d -p 6379:6379 redis --requirepass ""
# verify that redis is listening on 6379 port
nc  -zv  127.0.0.1 6379
```

## Start eHealth App

### start 'exams'

Start dapr runtime and 'exams' one by one in your terminal :

```bash
# start dapr runtime without 'exams'
dapr run --app-port 8081 --app-id exams --app-protocol http --dapr-http-port 3500 --dapr-grpc-port 50001 --dapr-http-read-buffer-size 16 --resources-path=./resources

# start 'exams' in another terminal
cd /ehealth/exams
./target/exams-1.0.5-SNAPSHOT-runner
```

Check the log to see if 'exams' and dapr runtime start successfully. 

### start 'permissions'

Start dapr runtime and 'permissions' one by one in your terminal :

```bash
# start dapr runtime without 'permissions'
dapr run --app-port 8082 --app-id permissions --app-protocol http --dapr-http-port 3502 --dapr-grpc-port 50002 --dapr-http-read-buffer-size 16 --resources-path=./resources

# start 'permissions' in another terminal
cd ehealth/permissions
./target/permissions-1.0.5-SNAPSHOT-runner
```
### start 'notifications'

Start dapr runtime and 'notifications' one by one in your terminal :

```bash
# start dapr runtime without 'notifications'
dapr run --app-port 8083 --app-id notifications --app-protocol http --dapr-http-port 3503 --dapr-grpc-port 50003 --dapr-http-read-buffer-size 16 --resources-path=./resources

# start 'notifications' in another terminal
cd ehealth/notifications
./target/notifications-1.0.5-SNAPSHOT-runner
```
### start 'notifications-email'

Start dapr runtime and 'notifications-email' one by one in your terminal :

```bash
# start dapr runtime without 'notifications-email'
dapr run --app-port 8084 --app-id notifications-email --app-protocol http --dapr-http-port 3504 --dapr-grpc-port 50004 --dapr-http-read-buffer-size 16 --resources-path=./resources

# start 'notifications-email' in another terminal
cd ehealth/notifications-email
./target/notifications-email-1.0.5-SNAPSHOT-runner
```
### start 'users'

Start dapr runtime and 'users' one by one in your terminal :

```bash
# start dapr runtime without 'users'
dapr run --app-port 8085 --app-id users --app-protocol http --dapr-http-port 3505 --dapr-grpc-port 50005 --dapr-http-read-buffer-size 16 --resources-path=./resources

# start 'users' in another terminal
cd ehealth/users
./target/users-1.0.5-SNAPSHOT-runner
```