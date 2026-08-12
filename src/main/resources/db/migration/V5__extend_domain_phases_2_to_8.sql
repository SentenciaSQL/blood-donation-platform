-- Phase 2-8 domain extensions (refresh tokens, donor profile, banks, inventory, requests, donations)

CREATE TABLE refresh_tokens (
    id          bigserial primary key,
    user_id     bigint       not null references users(id) on delete cascade,
    token       varchar(500) not null unique,
    expires_at  timestamp    not null,
    revoked     boolean      not null default false,
    created_at  timestamp    not null default now()
);

CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens(user_id);
CREATE INDEX refresh_tokens_expires_at_idx ON refresh_tokens(expires_at);

ALTER TABLE users
    ADD COLUMN updated_at timestamp;

-- Donor profile extensions
ALTER TABLE donors
    ADD COLUMN birth_date date,
    ADD COLUMN gender varchar(20),
    ADD COLUMN phone varchar(40),
    ADD COLUMN city varchar(120),
    ADD COLUMN address varchar(240),
    ADD COLUMN weight numeric(5, 2),
    ADD COLUMN eligible boolean not null default true,
    ADD COLUMN active boolean not null default true,
    ADD COLUMN created_at timestamp not null default now(),
    ADD COLUMN updated_at timestamp;

CREATE INDEX donors_city_idx ON donors(city);
CREATE INDEX donors_blood_type_city_idx ON donors(blood_type, city);
CREATE INDEX donors_eligible_active_idx ON donors(eligible, active);

-- Blood banks
ALTER TABLE blood_banks
    ADD COLUMN city varchar(120),
    ADD COLUMN email varchar(160),
    ADD COLUMN active boolean not null default true,
    ADD COLUMN updated_at timestamp;

CREATE INDEX blood_banks_city_idx ON blood_banks(city);
CREATE INDEX blood_banks_active_idx ON blood_banks(active);

UPDATE blood_banks
SET city = 'Santo Domingo'
WHERE name = 'Banco Nacional de Sangre' AND city IS NULL;

UPDATE blood_banks
SET city = 'Santiago'
WHERE name = 'Banco Central de Donación' AND city IS NULL;

-- Inventory
ALTER TABLE inventories
    ADD COLUMN minimum_stock integer not null default 5;

CREATE INDEX inventories_blood_type_idx ON inventories(blood_type);

-- Requests: expand model and migrate status/urgency
ALTER TABLE requests DROP CONSTRAINT IF EXISTS requests_status_chk;

UPDATE requests SET status = 'PENDING' WHERE status = 'OPEN';
UPDATE requests SET status = 'FULFILLED' WHERE status = 'CLOSED';

ALTER TABLE requests RENAME COLUMN hospital TO hospital_name;

ALTER TABLE requests
    ADD COLUMN units_required integer not null default 1,
    ADD COLUMN patient_name varchar(160),
    ADD COLUMN contact_phone varchar(40),
    ADD COLUMN city varchar(120),
    ADD COLUMN description text,
    ADD COLUMN required_date date,
    ADD COLUMN updated_at timestamp,
    ADD COLUMN urgency_code varchar(20);

UPDATE requests
SET urgency_code = CASE
                       WHEN urgency >= 3 THEN 'CRITICAL'
                       WHEN urgency = 2 THEN 'HIGH'
                       WHEN urgency = 1 THEN 'MEDIUM'
                       ELSE 'LOW'
                   END;

ALTER TABLE requests DROP COLUMN urgency;
ALTER TABLE requests RENAME COLUMN urgency_code TO urgency;
ALTER TABLE requests ALTER COLUMN urgency SET DEFAULT 'MEDIUM';
ALTER TABLE requests ALTER COLUMN urgency SET NOT NULL;

ALTER TABLE requests
    ADD CONSTRAINT requests_status_chk
        CHECK (status IN ('PENDING', 'MATCHING', 'MATCHED', 'FULFILLED', 'CANCELLED', 'REJECTED'));

ALTER TABLE requests
    ADD CONSTRAINT requests_urgency_chk
        CHECK (urgency IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

CREATE INDEX requests_city_idx ON requests(city);
CREATE INDEX requests_urgency_idx ON requests(urgency);
CREATE INDEX requests_created_at_idx ON requests(created_at);
CREATE INDEX requests_status_idx ON requests(status);

-- Donations: appointment fields and expanded statuses
ALTER TABLE donations DROP CONSTRAINT IF EXISTS donations_status_chk;

ALTER TABLE donations RENAME COLUMN scheduled_at TO appointment_date;

ALTER TABLE donations
    ADD COLUMN blood_type varchar(3),
    ADD COLUMN units_collected integer,
    ADD COLUMN notes text,
    ADD COLUMN updated_at timestamp;

UPDATE donations d
SET blood_type = dn.blood_type
FROM donors dn
WHERE dn.user_id = d.donor_user_id
  AND d.blood_type IS NULL;

ALTER TABLE donations
    ADD CONSTRAINT donations_status_chk
        CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'));

ALTER TABLE donations
    ADD CONSTRAINT donations_blood_type_chk
        CHECK (blood_type IS NULL OR blood_type IN ('O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'));

CREATE INDEX donations_appointment_date_idx ON donations(appointment_date);
CREATE INDEX donations_status_idx ON donations(status);
