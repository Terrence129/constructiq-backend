CREATE TABLE risks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    probability INTEGER NOT NULL,
    impact INTEGER NOT NULL,
    severity INTEGER NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    mitigation_plan TEXT,
    owner VARCHAR(100),
    target_date DATE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_risks_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_risks_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT ck_risks_category
        CHECK (category IN (
            'SAFETY',
            'SCHEDULE',
            'COST',
            'QUALITY',
            'DESIGN',
            'PROCUREMENT',
            'ENVIRONMENT',
            'LEGAL',
            'GENERAL'
        )),

    CONSTRAINT ck_risks_probability
        CHECK (probability BETWEEN 1 AND 5),

    CONSTRAINT ck_risks_impact
        CHECK (impact BETWEEN 1 AND 5),

    CONSTRAINT ck_risks_severity
        CHECK (severity = probability * impact),

    CONSTRAINT ck_risks_risk_level
        CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    CONSTRAINT ck_risks_status
        CHECK (status IN ('OPEN', 'MITIGATING', 'MONITORING', 'CLOSED'))
);

CREATE INDEX idx_risks_project_id ON risks(project_id);
CREATE INDEX idx_risks_created_by ON risks(created_by);
CREATE INDEX idx_risks_risk_level ON risks(risk_level);
CREATE INDEX idx_risks_status ON risks(status);
