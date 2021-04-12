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
@Table(name = "guest")
public class Guest {
    @Id
    @Column(name = "guest_id")
    private UUID id;

    @Column(name = "guest_code")
    private Long code;

    @Column(name = "segment_id")
    private UUID segmentId;

    @Column(name = "is_frozen")
    private Boolean isFrozen;

    @Column(name = "comm_status")
    private String communicationStatus;

    public Guest(Long code, UUID segmentId) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.segmentId = segmentId;
    }

    public Boolean isFrozen() {
        return Boolean.TRUE.equals(this.isFrozen);
    }
}
