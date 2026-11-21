#!/usr/bin/env bash

set -e
cd "$(dirname "$0")"

set -a
source "${1:-.env.local}"
set +a

DOMAIN="${GLASSFISH_DOMAIN:-domain1}"
ASADMIN="$GLASSFISH_HOME/bin/asadmin"
DOMAINS_DIR="${GLASSFISH_DOMAINS_DIR:-$GLASSFISH_HOME/domains}"
DOMAIN_LIB="$DOMAINS_DIR/$DOMAIN/lib"

if [[ "${SKIP_BUILD:-0}" != 1 ]]; then
    ./gradlew war copyGlassfishJdbcLibs
fi

postgres_jars=(build/glassfish/lib/postgresql-*.jar)
druid_jars=(build/glassfish/lib/druid-*.jar)
if [[ ! -f build/libs/lab1.war \
    || ${#postgres_jars[@]} != 1 || ! -f "${postgres_jars[0]}" \
    || ${#druid_jars[@]} != 1 || ! -f "${druid_jars[0]}" ]]; then
    echo 'Missing or ambiguous deployment artifacts. Run ./gradlew war copyGlassfishJdbcLibs first.' >&2
    exit 1
fi

"$ASADMIN" stop-domain --domaindir "$DOMAINS_DIR" "$DOMAIN" 2>/dev/null || true
cp "${postgres_jars[0]}" "${druid_jars[0]}" "$DOMAIN_LIB/"
"$ASADMIN" start-domain --domaindir "$DOMAINS_DIR" "$DOMAIN"

"$ASADMIN" undeploy lab1 2>/dev/null || true
"$ASADMIN" delete-jdbc-resource jdbc/lab1Serializable 2>/dev/null || true
"$ASADMIN" delete-jdbc-resource jdbc/lab1 2>/dev/null || true
"$ASADMIN" delete-jdbc-connection-pool Lab1SerializablePool 2>/dev/null || true
"$ASADMIN" delete-jdbc-connection-pool Lab1Pool 2>/dev/null || true

escape_jdbc_property() {
    local value="$1"
    value="${value//\\/\\\\}"
    value="${value//=/\\=}"
    printf '%s' "${value//:/\\:}"
}

jdbc_url=$(escape_jdbc_property "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME")
db_user=$(escape_jdbc_property "$DB_USER")
db_password=$(escape_jdbc_property "$DB_PASSWORD")
connection_properties=$(escape_jdbc_property "user=$DB_USER")
druid_properties="driverClassName=org.postgresql.Driver:url=$jdbc_url:user=$db_user:password=$db_password"
druid_properties+=":connectionProperties=$connection_properties"
druid_properties+=":initialSize=1:minIdle=1:maxActive=8:maxWait=5000:connectTimeout=5000"
druid_properties+=":validationQuery=SELECT 1:validationQueryTimeout=2:testWhileIdle=true"
druid_properties+=":timeBetweenEvictionRunsMillis=60000:minEvictableIdleTimeMillis=300000"

create_druid_resource() {
    local pool_name="$1" resource_name="$2" isolation="$3" jdbc_isolation="$4"

    "$ASADMIN" create-jdbc-connection-pool \
        --datasourceclassname com.alibaba.druid.pool.DruidDataSource \
        --restype javax.sql.DataSource \
        --pooling=false \
        --idletimeout=0 \
        --isolationlevel "$isolation" \
        --isisolationguaranteed=true \
        --property "name=$pool_name:$druid_properties:defaultTransactionIsolation=$jdbc_isolation" \
        "$pool_name"

    "$ASADMIN" create-jdbc-resource --connectionpoolid "$pool_name" "$resource_name"
}

create_druid_resource Lab1Pool jdbc/lab1 read-committed 2
create_druid_resource Lab1SerializablePool jdbc/lab1Serializable serializable 8

"$ASADMIN" ping-connection-pool Lab1Pool
"$ASADMIN" ping-connection-pool Lab1SerializablePool
"$ASADMIN" deploy --name lab1 --contextroot /lab1 build/libs/lab1.war

echo "Application API path: /lab1/api"
