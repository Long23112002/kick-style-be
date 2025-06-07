-- Tạo schema contacts nếu chưa có
CREATE SCHEMA IF NOT EXISTS contacts;

-- Tạo bảng contacts
CREATE TABLE contacts.contacts
(
    id             BIGSERIAL PRIMARY KEY,
    full_name      VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(20),
    address        VARCHAR(500),
    subject        VARCHAR(255),
    message        TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' 
                   CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    priority       VARCHAR(20)  DEFAULT 'NORMAL' 
                   CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    assigned_to    BIGINT,      -- ID của admin được giao nhiệm vụ
    resolved_at    TIMESTAMP,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_deleted     BOOLEAN      DEFAULT FALSE
);

-- Tạo bảng contact_replies
CREATE TABLE contacts.contact_replies
(
    id             BIGSERIAL PRIMARY KEY,
    contact_id     BIGINT       NOT NULL,
    admin_id       BIGINT       NOT NULL,
    reply_message  TEXT         NOT NULL,
    is_email_sent  BOOLEAN      DEFAULT FALSE,
    email_sent_at  TIMESTAMP,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_contact_replies_contact
        FOREIGN KEY (contact_id) REFERENCES contacts.contacts(id) ON DELETE CASCADE
);

-- Tạo indexes
CREATE INDEX idx_contacts_status ON contacts.contacts(status);
CREATE INDEX idx_contacts_created_at ON contacts.contacts(created_at);
CREATE INDEX idx_contacts_assigned_to ON contacts.contacts(assigned_to);
CREATE INDEX idx_contacts_email ON contacts.contacts(email);
CREATE INDEX idx_contact_replies_contact_id ON contacts.contact_replies(contact_id);
CREATE INDEX idx_contact_replies_admin_id ON contacts.contact_replies(admin_id);

-- Tạo trigger để tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION contacts.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_contacts_updated_at 
    BEFORE UPDATE ON contacts.contacts 
    FOR EACH ROW EXECUTE FUNCTION contacts.update_updated_at_column(); 