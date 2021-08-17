alter table segment rename column cacheback to cashback;
alter table segment rename column max_reward to max_benefit;

alter table segment add column if not exists zero_name_category text,
    add column if not exists first_name_category text,
    add column if not exists second_name_category text,
    add column if not exists text_slip_check text,
    add column if not exists plu_count integer,
    add column if not exists plu_list text;

drop table if exists template_definition;