package process_manager.repository;

import process_manager.entity.Guest;
import process_manager.entity.GuestCommunicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {

    List<Guest> findAllBySegmentId(UUID segmentId);

    @Query("select code from Guest where segmentId = :segmentId ")
    List<Long> getCodesBySegmentId(@Param("segmentId") UUID segmentId);

    @Query("select code from Guest where segmentId = :segmentId and isFrozen = true ")
    List<Long> getFrozenCodesBySegmentId(@Param("segmentId") UUID segmentId);

    @Modifying
    @Query("update Guest set isFrozen = :isFrozen where code = :code and segmentId = :segmentId")
    void setFrozen(@Param(value = "segmentId") UUID segmentId,
                   @Param(value = "code") Long code,
                   @Param(value = "isFrozen") Boolean isFrozen);

    @Modifying
    @Query("update Guest set communicationStatus = :status, deferredDate = :deferredDate " +
            "where code = :code and segmentId = :segmentId")
    void setUpcStatus(@Param(value = "segmentId") UUID segmentId,
                      @Param(value = "code") Long code,
                      @Param(value = "status") GuestCommunicationStatus status,
                      @Param(value = "deferredDate") LocalDateTime deferredDate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into guest (guest_code, segment_id) " +
            "select cast(cast(unnest(ARRAY(SELECT json_array_elements(json_build_array :codes))) as text) as bigint), :segmentId",
            nativeQuery = true)
    void insertBatch(@Param("segmentId") UUID segmentId, @Param("codes") List<Long> codes);
}
