#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker-compose exec -T postgres pg_isready -U object_accounting; do
  sleep 1
done

echo "PostgreSQL is ready. Adding test data..."

# Add test customer
docker-compose exec -T postgres psql -U object_accounting -d object_accounting << EOSQL
INSERT INTO customer (name, legal_address, created_at, updated_at, version)
VALUES ('ООО "СтройМастер"', 'г. Минск, ул. Строителей, д. 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;
EOSQL

# Add test employee
docker-compose exec -T postgres psql -U object_accounting -d object_accounting << EOSQL
INSERT INTO employee (phone_number, full_name, position, customer_id, created_at, updated_at, version)
VALUES ('+375291234567', 'Иванов Иван Иванович', 'Инженер', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;
EOSQL

echo "Test data added successfully!"
