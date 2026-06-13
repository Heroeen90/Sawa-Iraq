-- Sawa Iraq Super App SaaS PostgreSQL Schema
-- Authorized for deployment on commercial cloud environments (Azure, GCP, AWS)

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER', -- 'CUSTOMER', 'PROVIDER', 'ADMIN'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS providers (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    service_type VARCHAR(50) NOT NULL, -- 'TAXI', 'TOKTOK', 'TOWING', 'GAS', 'WATER', 'VENDOR'
    sub_category VARCHAR(100) DEFAULT '',
    vehicle_plate VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL DEFAULT 'Baghdad',
    is_available BOOLEAN DEFAULT TRUE,
    rating NUMERIC(3,2) DEFAULT 5.00,
    subscription_expiry TIMESTAMP WITH TIME ZONE,
    wallet_balance NUMERIC(12,2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    service_type VARCHAR(50) NOT NULL,
    customer_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    provider_id INTEGER REFERENCES providers(id) ON DELETE SET NULL,
    start_location VARCHAR(255) NOT NULL,
    end_location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ACCEPTED', 'ON_THE_WAY', 'COMPLETED', 'CANCELLED'
    price NUMERIC(12,2) NOT NULL,
    distance_km NUMERIC(6,2) NOT NULL,
    commission_iqd NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH', -- 'CASH', 'ZAIN_CASH', 'ASIA_PAY'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id SERIAL PRIMARY KEY,
    provider_id INTEGER REFERENCES providers(id) ON DELETE CASCADE,
    amount NUMERIC(12,2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- 'COMMISSION', 'SUBSCRIPTION', 'PAYOUT', 'RIDE_SHARE'
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    action_performed VARCHAR(255) NOT NULL,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Pre-seed main SaaS configuration table/settings
CREATE TABLE IF NOT EXISTS system_contracts (
    id VARCHAR(50) PRIMARY KEY,
    commission_rate_taxi NUMERIC(4,2) DEFAULT 10.00,
    commission_rate_toktok NUMERIC(4,2) DEFAULT 5.00,
    monthly_subscription_iqd NUMERIC(12,2) DEFAULT 15000.00,
    system_status VARCHAR(50) DEFAULT 'ACTIVE'
);

INSERT INTO system_contracts (id, commission_rate_taxi, commission_rate_toktok, monthly_subscription_iqd, system_status)
VALUES ('active_contract', 10.00, 5.00, 15000.00, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
