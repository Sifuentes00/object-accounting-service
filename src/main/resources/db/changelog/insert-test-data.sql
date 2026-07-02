-- Liquibase changeset for initial data
-- changeset matvey:insert-initial-customer-1
INSERT INTO customer (name, legal_address, created_at, updated_at, version)
VALUES ('БЕЛСПЕЦЭНЕРГО', 'г. Минск', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- rollback DELETE FROM customer WHERE name = 'БЕЛСПЕЦЭНЕРГО';
