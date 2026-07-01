-- Liquibase changeset for test data
-- changeset matvey:insert-test-customer-1
INSERT INTO customer (name, legal_address, created_at, updated_at, version)
VALUES ('ООО "СтройМастер"', 'г. Минск, ул. Строителей, д. 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- changeset matvey:insert-test-employee-1
INSERT INTO employee (phone_number, full_name, position, customer_id, created_at, updated_at, version)
VALUES ('+375291234567', 'Иванов Иван Иванович', 'Инженер', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- rollback DELETE FROM employee WHERE phone_number = '+375291234567';
-- rollback DELETE FROM customer WHERE name = 'ООО "СтройМастер"';
