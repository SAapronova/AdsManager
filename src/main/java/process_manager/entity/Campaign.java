package process_manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Entity
@Table(name = "campaign")
public class Campaign {
    @Id
    @Column(name = "campaign_id")
    private UUID id;

    @Column(name = "campaign_code")
    private String campaignCode;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(name = "post_period_end")
    private LocalDateTime postPeriodEnd;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id", updatable = false, insertable = false)
    private List<Segment> segments = new ArrayList<>();

    @Column(name = "launch_count")
    private Integer launchCount;
}
