CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    legal_address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    position VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_employee_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

CREATE TABLE object (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    work_type VARCHAR(50) NOT NULL,
    image_unique_name VARCHAR(255),
    customer_id BIGINT NOT NULL,
    responsible_employee_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_object_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT,
    CONSTRAINT fk_object_responsible_employee FOREIGN KEY (responsible_employee_id) REFERENCES employee(id) ON DELETE SET NULL,
    CONSTRAINT uk_object_responsible_employee UNIQUE (responsible_employee_id),
    CONSTRAINT chk_work_type CHECK (work_type IN ('DESIGN', 'GEODESY', 'CONSTRUCTION_INSTALLATION'))
);

CREATE TABLE contract (
    id BIGSERIAL PRIMARY KEY,
    conclusion_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number VARCHAR(100) NOT NULL,
    file_unique_name VARCHAR(255) NOT NULL,
    object_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_contract_object FOREIGN KEY (object_id) REFERENCES object(id) ON DELETE CASCADE,
    CONSTRAINT uk_contract_number UNIQUE (number)
);

CREATE TABLE ppr (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    archive_number VARCHAR(100) NOT NULL,
    number VARCHAR(100) NOT NULL,
    file_unique_name VARCHAR(255) NOT NULL,
    object_id BIGINT NOT NULL,
    employee_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ppr_object FOREIGN KEY (object_id) REFERENCES object(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppr_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE SET NULL,
    CONSTRAINT uk_ppr_number UNIQUE (number)
);

CREATE INDEX idx_object_customer_id ON object(customer_id);
CREATE INDEX idx_object_responsible_employee_id ON object(responsible_employee_id);
CREATE INDEX idx_contract_object_id ON contract(object_id);
CREATE INDEX idx_employee_customer_id ON employee(customer_id);
CREATE INDEX idx_ppr_object_id ON ppr(object_id);
CREATE INDEX idx_ppr_employee_id ON ppr(employee_id);
