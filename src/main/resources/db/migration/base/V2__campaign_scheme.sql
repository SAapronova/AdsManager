create table campaign (
                          campaign_id uuid primary key,
                          campaign_code text,
                          period_start timestamp,
                          period_end timestamp,
                          post_period_end timestamp,
                          create_time timestamp,
                          status text
);

create table segment (
                         segment_id uuid primary key,
                         campaign_id uuid references campaign (campaign_id),
                         segment_type text,
                         channel_type text,
                         content_text text,
                         content_link text,
                         content_link_text text,
                         image_url text,
                         offer_template text,
                         points int,
                         multiplier int,
                         discount int,
                         cacheback int,
                         min_sum int,
                         purchases_num int,
                         reward_period int,
                         max_reward int,
                         is_rule_on boolean,
                         is_segment_on boolean,
                         rule_code text,
                         test_phones text,
                         is_upc boolean
);

create index idx_segment_campaign_id on segment (campaign_id);

create table guest (
                       guest_id uuid primary key,
                       guest_code bigint,
                       segment_id uuid references segment (segment_id),
                       is_frozen boolean,
                       comm_status text
);

create index idx_guest_segment_id on guest (segment_id);
create index idx_guest_guest_code_segment_id on guest (guest_code, segment_id);