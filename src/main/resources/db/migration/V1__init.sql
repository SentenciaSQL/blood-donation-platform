create table users (
                       id          bigserial primary key,
                       name        varchar(120) not null,
                       email       varchar(160) not null unique,
                       password    varchar(200) not null,
                       phone       varchar(40),
                       role        varchar(20)  not null, -- DONOR|REQUESTER|ADMIN
                       enabled     boolean      not null default true,
                       created_at  timestamp    not null default now()
);

create table donors (
                        user_id         bigint primary key references users(id) on delete cascade,
                        blood_type      varchar(3) not null,
                        last_donation_at timestamp,
                        availability    boolean    not null default true,
                        latitude        numeric(9,6),
                        longitude       numeric(9,6),
                        constraint donors_blood_type_chk check (blood_type in ('O-','O+','A-','A+','B-','B+','AB-','AB+'))
);

create table blood_banks (
                             id         bigserial primary key,
                             name       varchar(160) not null,
                             address    varchar(240),
                             phone      varchar(40),
                             latitude   numeric(9,6),
                             longitude  numeric(9,6),
                             created_at timestamp not null default now()
);

create table inventories (
                             id              bigserial primary key,
                             blood_bank_id   bigint not null references blood_banks(id) on delete cascade,
                             blood_type      varchar(3) not null,
                             units_available integer not null default 0,
                             updated_at      timestamp not null default now(),
                             constraint inventories_blood_type_chk check (blood_type in ('O-','O+','A-','A+','B-','B+','AB-','AB+')),
                             constraint inventories_unique unique (blood_bank_id, blood_type)
);

create table requests (
                          id                    bigserial primary key,
                          requester_user_id     bigint not null references users(id) on delete restrict,
                          blood_type            varchar(3) not null,
                          urgency               smallint  not null default 1, -- 1..3
                          hospital              varchar(200),
                          latitude              numeric(9,6),
                          longitude             numeric(9,6),
                          status                varchar(16) not null default 'OPEN',
                          matched_donor_user_id bigint null references users(id) on delete set null,
                          created_at            timestamp not null default now(),
                          constraint requests_blood_type_chk check (blood_type in ('O-','O+','A-','A+','B-','B+','AB-','AB+')),
                          constraint requests_status_chk check (status in ('OPEN','MATCHED','CLOSED'))
);
create index requests_status_blood_type_idx on requests(status, blood_type);

create table donations (
                           id            bigserial primary key,
                           donor_user_id bigint not null references users(id) on delete restrict,
                           blood_bank_id bigint not null references blood_banks(id) on delete restrict,
                           request_id    bigint null references requests(id) on delete set null,
                           scheduled_at  timestamp not null,
                           status        varchar(16) not null default 'SCHEDULED',
                           created_at    timestamp not null default now(),
                           constraint donations_status_chk check (status in ('SCHEDULED','COMPLETED','CANCELLED'))
);
create index donations_donor_status_idx on donations(donor_user_id, status);

create table notifications (
                               id         bigserial primary key,
                               user_id    bigint not null references users(id) on delete cascade,
                               title      varchar(160) not null,
                               body       text not null,
                               seen       boolean not null default false,
                               created_at timestamp not null default now()
);

create index donors_blood_type_idx on donors(blood_type);
create index donors_location_idx on donors(latitude, longitude);
