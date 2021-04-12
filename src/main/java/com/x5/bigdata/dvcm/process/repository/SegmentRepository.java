package com.x5.bigdata.dvcm.process.repository;

import com.x5.bigdata.dvcm.process.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, UUID> {
}
