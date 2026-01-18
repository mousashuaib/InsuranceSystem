-- Migration V2: Coverage and Claim Enhancements
-- Healthcare Insurance System - January 2026
-- This migration adds new columns and tables for the enhanced claim workflow

-- ============================================================
-- 1. Add new columns to coverages table
-- ============================================================
ALTER TABLE coverages ADD COLUMN IF NOT EXISTS allowed_gender VARCHAR(20) DEFAULT 'ALL';
ALTER TABLE coverages ADD COLUMN IF NOT EXISTS min_age INTEGER;
ALTER TABLE coverages ADD COLUMN IF NOT EXISTS max_age INTEGER;
ALTER TABLE coverages ADD COLUMN IF NOT EXISTS frequency_limit INTEGER;
ALTER TABLE coverages ADD COLUMN IF NOT EXISTS frequency_period VARCHAR(20);

-- ============================================================
-- 2. Add new columns to healthcare_provider_claims table
-- ============================================================
ALTER TABLE healthcare_provider_claims ADD COLUMN IF NOT EXISTS is_chronic BOOLEAN DEFAULT false;
ALTER TABLE healthcare_provider_claims ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP;
ALTER TABLE healthcare_provider_claims ADD COLUMN IF NOT EXISTS paid_by UUID;
ALTER TABLE healthcare_provider_claims ADD COLUMN IF NOT EXISTS is_follow_up BOOLEAN DEFAULT false;
-- Update any NULL values to false
UPDATE healthcare_provider_claims SET is_follow_up = false WHERE is_follow_up IS NULL;
UPDATE healthcare_provider_claims SET is_chronic = false WHERE is_chronic IS NULL;

-- ============================================================
-- 3. Create provider_policies table
-- ============================================================
CREATE TABLE IF NOT EXISTS provider_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL,
    service_name VARCHAR(160) NOT NULL,
    negotiated_price DECIMAL(12,2),
    coverage_percent DECIMAL(5,2),
    effective_from DATE,
    effective_to DATE,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_provider_policies_provider FOREIGN KEY (provider_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_provider_policies_provider_service UNIQUE(provider_id, service_name)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_provider_policies_provider_id ON provider_policies(provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_policies_active ON provider_policies(active);

-- ============================================================
-- 4. Create annual_usage table
-- ============================================================
CREATE TABLE IF NOT EXISTS annual_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    year INTEGER NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    total_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_annual_usage_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_annual_usage_client_year_service UNIQUE(client_id, year, service_type)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_annual_usage_client_id ON annual_usage(client_id);
CREATE INDEX IF NOT EXISTS idx_annual_usage_year ON annual_usage(year);

-- ============================================================
-- 5. Create coverage_usage table
-- ============================================================
CREATE TABLE IF NOT EXISTS coverage_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    provider_specialization VARCHAR(100),
    service_type VARCHAR(50),
    usage_date DATE NOT NULL,
    year INTEGER NOT NULL,
    visit_count INTEGER DEFAULT 1,
    amount_used DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coverage_usage_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_coverage_usage_client_spec_date UNIQUE(client_id, provider_specialization, usage_date)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_coverage_usage_client_id ON coverage_usage(client_id);
CREATE INDEX IF NOT EXISTS idx_coverage_usage_date ON coverage_usage(usage_date);
CREATE INDEX IF NOT EXISTS idx_coverage_usage_year ON coverage_usage(year);

-- ============================================================
-- 6. Update existing claim statuses if needed
-- ============================================================
-- Note: The ClaimStatus enum values are managed by Hibernate
-- New values added: RETURNED_TO_PROVIDER, PAYMENT_PENDING, PAID

-- ============================================================
-- End of Migration V2
-- ============================================================
