#!/usr/bin/env bash

set -e
cd "$(dirname "$0")"

set -a
source "${1:-.env.local}"
set +a

DOMAIN="${GLASSFISH_DOMAIN:-domain1}"
ASADMIN="$GLASSFISH_HOME/bin/asadmin"
DOMAIN_LIB="$GLASSFISH_HOME/domains/$DOMAIN/lib"

./gradlew clean war copyPostgresqlDriver

"$ASADMIN" stop-domain "$DOMAIN" 2>/dev/null || true
cp build/glassfish/lib/postgresql-*.jar "$DOMAIN_LIB/"
"$ASADMIN" start-domain "$DOMAIN"

"$ASADMIN" undeploy lab1 2>/dev/null || true
"$ASADMIN" delete-jdbc-resource jdbc/lab1 2>/dev/null || true
"$ASADMIN" delete-jdbc-connection-pool Lab1Pool 2>/dev/null || true

"$ASADMIN" create-jdbc-connection-pool \
    --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
    --restype javax.sql.DataSource \
    --property "serverName=$DB_HOST:portNumber=$DB_PORT:databaseName=$DB_NAME:user=$DB_USER:password=$DB_PASSWORD" \
    Lab1Pool

"$ASADMIN" create-jdbc-resource \
    --connectionpoolid Lab1Pool jdbc/lab1

"$ASADMIN" ping-connection-pool Lab1Pool
"$ASADMIN" deploy --name lab1 --contextroot /lab1 build/libs/lab1.war

echo "http://localhost:8080/lab1/api"
