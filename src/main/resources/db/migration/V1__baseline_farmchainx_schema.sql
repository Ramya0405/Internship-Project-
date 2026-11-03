CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    email VARCHAR(150) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) DEFAULT NULL,
    role ENUM('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER','CONSUMER') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_6dotkott2kjsp8vw4d0m25fb7 (email),
    KEY idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    product_image VARCHAR(255) DEFAULT NULL,
    product_name VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_product_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS farms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    certification VARCHAR(200) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    farm_name VARCHAR(150) NOT NULL,
    farm_size DOUBLE DEFAULT NULL,
    location VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_farm_owner (owner_id),
    CONSTRAINT FK_farms_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS product_batches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    current_status ENUM('CREATED','IN_TRANSIT','IN_WAREHOUSE','QUALITY_INSPECTED','AT_RETAILER','SOLD','RECALLED','EXPIRED') NOT NULL,
    expiry_date DATE DEFAULT NULL,
    harvest_date DATE NOT NULL,
    quantity DOUBLE NOT NULL,
    tracking_id VARCHAR(30) NOT NULL,
    farm_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_batch_tracking_id (tracking_id),
    KEY idx_batch_farm (farm_id),
    KEY idx_batch_farmer (farmer_id),
    KEY idx_batch_status (current_status),
    KEY idx_batch_product (product_id),
    CONSTRAINT FK_product_batches_farm FOREIGN KEY (farm_id) REFERENCES farms (id),
    CONSTRAINT FK_product_batches_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT FK_product_batches_farmer FOREIGN KEY (farmer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ai_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    analyzed_at DATETIME(6) NOT NULL,
    detected_anomalies TEXT,
    recommendations TEXT,
    risk_level ENUM('LOW','MEDIUM','HIGH') NOT NULL,
    risk_score INT NOT NULL,
    batch_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_batch (batch_id),
    KEY idx_ai_risk_level (risk_level),
    KEY idx_ai_analyzed_at (analyzed_at),
    CONSTRAINT FK_ai_analysis_batch FOREIGN KEY (batch_id) REFERENCES product_batches (id),
    CONSTRAINT ai_analysis_score_range CHECK (risk_score <= 100 AND risk_score >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    is_read BIT(1) NOT NULL,
    message VARCHAR(500) NOT NULL,
    notification_type ENUM('INFO','WARNING','ALERT','SUCCESS') NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notif_user (user_id),
    KEY idx_notif_is_read (is_read),
    KEY idx_notif_created (created_at),
    CONSTRAINT FK_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS quality_inspections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    humidity DOUBLE DEFAULT NULL,
    inspection_date DATE NOT NULL,
    inspection_notes VARCHAR(1000) DEFAULT NULL,
    quality_grade ENUM('A_PLUS','A','B','C','REJECTED') NOT NULL,
    temperature DOUBLE DEFAULT NULL,
    batch_id BIGINT NOT NULL,
    inspected_by_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_qi_batch (batch_id),
    KEY idx_qi_inspected_by (inspected_by_id),
    KEY idx_qi_grade (quality_grade),
    CONSTRAINT FK_quality_inspections_batch FOREIGN KEY (batch_id) REFERENCES product_batches (id),
    CONSTRAINT FK_quality_inspections_user FOREIGN KEY (inspected_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS supply_chain_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(500) DEFAULT NULL,
    event_timestamp DATETIME(6) NOT NULL,
    event_type ENUM('HARVESTED','PACKED','DISPATCHED','IN_TRANSIT','ARRIVED_AT_WAREHOUSE','STORED','QUALITY_CHECK','DISPATCHED_TO_RETAILER','ARRIVED_AT_RETAILER','SOLD','RECALLED','TEMPERATURE_ALERT','HUMIDITY_ALERT','DELAY_REPORTED','CUSTOMS_CLEARED','DAMAGED') NOT NULL,
    humidity DOUBLE DEFAULT NULL,
    location VARCHAR(255) DEFAULT NULL,
    temperature DOUBLE DEFAULT NULL,
    batch_id BIGINT NOT NULL,
    updated_by_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_sce_batch (batch_id),
    KEY idx_sce_timestamp (event_timestamp),
    KEY idx_sce_type (event_type),
    KEY idx_sce_updated_by (updated_by_id),
    CONSTRAINT FK_supply_chain_events_user FOREIGN KEY (updated_by_id) REFERENCES users (id),
    CONSTRAINT FK_supply_chain_events_batch FOREIGN KEY (batch_id) REFERENCES product_batches (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS transportation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actual_arrival DATETIME(6) DEFAULT NULL,
    departure_date DATETIME(6) DEFAULT NULL,
    destination_location VARCHAR(255) NOT NULL,
    estimated_arrival DATETIME(6) DEFAULT NULL,
    notes VARCHAR(500) DEFAULT NULL,
    source_location VARCHAR(255) NOT NULL,
    transportation_status ENUM('SCHEDULED','IN_PROGRESS','DELAYED','COMPLETED','CANCELLED') NOT NULL,
    vehicle_number VARCHAR(30) DEFAULT NULL,
    batch_id BIGINT NOT NULL,
    distributor_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_transport_batch (batch_id),
    KEY idx_transport_distributor (distributor_id),
    KEY idx_transport_status (transportation_status),
    CONSTRAINT FK_transportation_user FOREIGN KEY (distributor_id) REFERENCES users (id),
    CONSTRAINT FK_transportation_batch FOREIGN KEY (batch_id) REFERENCES product_batches (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    capacity_tons DOUBLE DEFAULT NULL,
    location VARCHAR(255) NOT NULL,
    warehouse_name VARCHAR(150) NOT NULL,
    manager_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_warehouse_manager (manager_id),
    CONSTRAINT FK_warehouses_manager FOREIGN KEY (manager_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
