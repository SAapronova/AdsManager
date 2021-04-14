create table template_definition (
                          template_definition_id uuid primary key,
                          offer_template text,
                          has_points boolean,
                          has_min_sum boolean,
                          has_reward_period boolean,
                          has_purchases_num boolean
);

insert into template_definition(template_definition_id, offer_template, has_points, has_min_sum, has_reward_period, has_purchases_num)
  values('453168ee-d4b9-4a86-9681-db8d00101955', 'TST_SAS_14', true, true, true, true);