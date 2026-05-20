

CREATE DATABASE IF NOT EXISTS family_contacts;
USE family_contacts;

-- ── Contacts ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS contacts (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    relationship VARCHAR(100),
    phone        VARCHAR(30),
    email        VARCHAR(150),
    address      VARCHAR(255),
    birthday     DATE,
    notes        TEXT,
    photo_path   VARCHAR(500),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── Groups ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS contact_groups (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL UNIQUE
);

-- ── Contact ↔ Group mapping ─────────────────────────────────
CREATE TABLE IF NOT EXISTS contact_group_map (
    contact_id INT,
    group_id   INT,
    PRIMARY KEY (contact_id, group_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id)   REFERENCES contact_groups(id) ON DELETE CASCADE
);

-- ── Default groups ───────────────────────────────────────────
INSERT IGNORE INTO contact_groups (group_name) VALUES
    ('Immediate Family'),
    ('Extended Family'),
    ('Friends'),
    ('Emergency Contacts');

-- ── Sample data (optional) ───────────────────────────────────
INSERT IGNORE INTO contacts (first_name, last_name, relationship, phone, email, birthday)
VALUES
    ('Park',  'Jisoo',  'Father',  '010-1234-5678', 'jisoo@email.com',  '1965-03-15'),
    ('Kim',   'Minji',  'Mother',  '010-2345-6789', 'minji@email.com',  '1968-07-22'),
    ('Park',  'Junho',  'Son',     '010-3456-7890', 'junho@email.com',  '1998-11-05'),
    ('Park',  'Jiyeon','Daughter', '010-4567-8901', 'jiyeon@email.com', '2002-04-18');

SELECT 'Setup complete!' AS Status;
