CREATE TABLE dashboard_statistics_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_projects BIGINT NOT NULL,
    active_projects BIGINT NOT NULL,
    completed_projects BIGINT NOT NULL,
    total_tasks BIGINT NOT NULL,
    open_tasks BIGINT NOT NULL,
    completed_tasks BIGINT NOT NULL,
    overdue_tasks BIGINT NOT NULL,
    total_risks BIGINT NOT NULL,
    open_risks BIGINT NOT NULL,
    high_risks BIGINT NOT NULL,
    critical_risks BIGINT NOT NULL,
    progress_reports BIGINT NOT NULL,
    documents BIGINT NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dashboard_statistics_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_dashboard_statistics_user_generated_at
    ON dashboard_statistics_snapshots(user_id, generated_at DESC);
