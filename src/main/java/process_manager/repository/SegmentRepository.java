package process_manager.repository;

import process_manager.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, UUID> {
}
