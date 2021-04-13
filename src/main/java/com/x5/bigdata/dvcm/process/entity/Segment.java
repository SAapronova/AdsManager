package com.x5.bigdata.dvcm.process.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Entity
@Table(name = "segment")
public class Segment {
    @Id
    @Column(name = "segment_id")
    private UUID id;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "segment_type")
    @Enumerated(EnumType.STRING)
    private SegmentType type;

    @Column(name = "channel_type")
    @Enumerated(EnumType.STRING)
    private ChannelType channelType;

    @Column(name = "content_text")
    private String contentText;

    @Column(name = "content_link")
    private String contentLink;

    @Column(name = "content_link_text")
    private String contentLinkText;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "offer_template")
    @Enumerated(EnumType.STRING)
    private OfferTemplate offerTemplate;

    @Column(name = "points")
    private Integer points;

    @Column(name = "multiplier")
    private Integer multiplier;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "cacheback")
    private Integer cacheBack;

    @Column(name = "min_sum")
    private Integer minSum;

    @Column(name = "purchases_num")
    private Integer purchases;

    @Column(name = "reward_period")
    private Integer rewardPeriod;

    @Column(name = "max_reward")
    private Integer maxReward;

    @Column(name = "is_rule_on")
    private Boolean isRuleOn;

    @Column(name = "is_segment_on")
    private Boolean isSegmentOn;

    @Column(name = "rule_code")
    private String ruleCode;

    @Column(name = "test_phones")
    private String testPhones;

    @Column(name = "is_upc")
    private Boolean isUpc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", updatable = false, insertable = false)
    private Campaign campaign;

    public boolean isRuleOn() {
        return Boolean.TRUE.equals(this.isRuleOn);
    }

    public boolean isSegmentOn() {
        return Boolean.TRUE.equals(this.isSegmentOn);
    }

    public void setTestPhones(List<Integer> phones) {
        this.testPhones = (phones == null || phones.isEmpty()) ? null :
                phones.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public List<Integer> getTestPhones() {
        return (this.testPhones == null) ? null :
                Arrays.stream(this.testPhones.split(","))
                        .filter(Predicate.not(String::isBlank))
                        .map(String::trim)
                        .mapToInt(Integer::parseInt)
                        .boxed()
                        .collect(Collectors.toList());
    }

    public boolean isUpc() {
        return Boolean.TRUE.equals(this.isUpc);
    }
}
