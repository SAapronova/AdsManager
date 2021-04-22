create sequence guest_seq;

alter table guest drop column guest_id;
alter table guest add column guest_id bigint default nextval('guest_seq');
update guest set guest_id = nextval('guest_seq');
alter table guest add primary key (guest_id);