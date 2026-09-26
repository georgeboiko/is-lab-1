docker-compose -f compose.yaml --env-file .env.local up -d
docker-compose -f compose.yaml --env-file .env.local exec -T db \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < migrations/schema.sql
docker-compose -f compose.yaml --env-file .env.local exec -T db \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < migrations/functions.sql