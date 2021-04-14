package com.x5.bigdata.dvcm.process.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "template_definition")
public class TemplateDefinition {
    @Id
    @Column(name = "template_definition_id")
    private UUID id;

    @Column(name = "offer_template")
    @Enumerated(EnumType.STRING)
    private OfferTemplate offerTemplate;

    @Column(name = "has_points")
    private Boolean hasPoints;

    @Column(name = "has_min_sum")
    private Boolean hasMinSum;

    @Column(name = "has_reward_period")
    private Boolean hasRewardPeriod;

    @Column(name = "has_purchases_num")
    private Boolean hasPurchases;
}
