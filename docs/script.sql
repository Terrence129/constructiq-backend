create table users
(
    id            bigserial
        primary key,
    name          varchar(100)                                  not null,
    email         varchar(150)                                  not null
        unique,
    password_hash varchar(255)                                  not null,
    role          varchar(30) default 'USER'::character varying not null,
    created_at    timestamp   default CURRENT_TIMESTAMP         not null,
    updated_at    timestamp
);

alter table users
    owner to postgres;

create table projects
(
    id          bigserial
        primary key,
    name        varchar(150)                        not null,
    description text,
    location    varchar(150),
    client_name varchar(150),
    status      varchar(30)                         not null,
    start_date  date,
    end_date    date,
    created_by  bigint                              not null
        constraint fk_projects_created_by
            references users,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp
);

alter table projects
    owner to postgres;

create table tasks
(
    id          bigserial
        primary key,
    project_id  bigint                              not null
        constraint fk_tasks_project
            references projects
            on delete cascade,
    title       varchar(150)                        not null,
    description text,
    status      varchar(30)                         not null,
    priority    varchar(30)                         not null,
    assignee    varchar(100),
    due_date    date,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp
);

alter table tasks
    owner to postgres;

create table progress_reports
(
    id             bigserial
        primary key,
    project_id     bigint                              not null
        constraint fk_progress_report_project
            references projects
            on delete cascade,
    report_date    date                                not null,
    summary        text                                not null,
    completed_work text,
    delayed_work   text,
    issues         text,
    next_actions   text,
    created_by     bigint                              not null
        constraint fk_progress_report_creator
            references users,
    created_at     timestamp default CURRENT_TIMESTAMP not null,
    updated_at     timestamp
);

alter table progress_reports
    owner to postgres;

create table user_project_registrations
(
    id          bigint                              not null
        constraint user_project_registrations_pk
            primary key,
    user_id     bigint                              not null
        constraint user_project_registrations_users_id_fk
            references users,
    project_id  bigint                              not null
        constraint user_project_registrations_projects_id_fk
            references projects,
    title       varchar(150),
    description text,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp
);

alter table user_project_registrations
    owner to postgres;


