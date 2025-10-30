-- === USERS DE PRUEBA ===
insert into users (first_name, last_name, email, password, role, active, created_at)
values
    ('Admin', 'User', 'admin@test.com',
     '$2a$10$O7Qk0TadE7gmLwZkBv06.eiIxv06hwkpi6Ac.WiEYqaHnSgq5rKHu', -- password: admin123
     'ADMIN', true, now()),

    ('Requester', 'User', 'req@test.com',
     '$2a$10$7RG0eYjU0Axy6sKUQDNx0eYrrUBpEBK5sNGWrc8Mb71OmylKTN4iK', -- password: req123
     'REQUESTER', true, now()),

    ('Donor', 'One', 'donor1@test.com',
     '$2a$10$80ELfxQqkk1nL5izddq8CeYlNEzWBdBQjcGMjoWBPNDjoLZxH4eXy', -- password: donor123
     'DONOR', true, now());

-- === DONANTES ===
insert into donors (user_id, blood_type, availability, latitude, longitude)
values
    ((select id from users where email='donor1@test.com'), 'O+', true, 18.4861, -69.9312);

-- === BANCOS DE SANGRE ===
insert into blood_banks (name, address, phone, latitude, longitude)
values
    ('Banco Nacional de Sangre', 'Av. Independencia 245, Santo Domingo', '+1 809 555 0000', 18.47, -69.90),
    ('Banco Central de Donación', 'Calle Duarte 101, Santiago', '+1 809 444 9999', 19.45, -70.70);

-- === INVENTARIOS ===
insert into inventories (blood_bank_id, blood_type, units_available)
select id, 'O+', 12 from blood_banks where name='Banco Nacional de Sangre';

insert into inventories (blood_bank_id, blood_type, units_available)
select id, 'A+', 8 from blood_banks where name='Banco Central de Donación';

-- === SOLICITUDES DE SANGRE ===
insert into requests (requester_user_id, blood_type, urgency, hospital, latitude, longitude, status, created_at)
values
    ((select id from users where email='req@test.com'), 'O+', 3, 'Hospital Central', 18.4862, -69.93, 'OPEN', now());

-- === DONACIONES ===
insert into donations (donor_user_id, blood_bank_id, request_id, scheduled_at, status, created_at)
values
    ((select id from users where email='donor1@test.com'),
     (select id from blood_banks where name='Banco Nacional de Sangre'),
     (select id from requests where requester_user_id = (select id from users where email='req@test.com')),
     now() + interval '2 days', 'SCHEDULED', now());

-- === NOTIFICACIONES ===
insert into notifications (user_id, title, body, seen, created_at)
values
    ((select id from users where email='donor1@test.com'),
     'Solicitud urgente de sangre O+',
     'Se requiere donante O+ en Hospital Central', false, now());
