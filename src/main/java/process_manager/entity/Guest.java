package process_manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "guest")
public class Guest {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long id;

    @Column(name = "guest_code")
    private Long code;

    @Column(name = "segment_id")
    private UUID segmentId;

    @Column(name = "is_frozen")
    private Boolean isFrozen;

    @Column(name = "comm_status")
    @Enumerated(EnumType.STRING)
    private GuestCommunicationStatus communicationStatus;

    @Column(name = "deferred_date")
    private LocalDateTime deferredDate;

    public Guest(Long code, UUID segmentId) {
        this.code = code;
        this.segmentId = segmentId;
    }

    public Boolean isFrozen() {
        return Boolean.TRUE.equals(this.isFrozen);
    }
}
