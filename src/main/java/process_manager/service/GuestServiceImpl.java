package process_manager.service;

import process_manager.dto.GuestDto;
import process_manager.entity.Guest;
import process_manager.entity.GuestCommunicationStatus;
import process_manager.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GuestServiceImpl implements GuestService {
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public void save(UUID segmentId, List<Long> guests) {
        guests = guests.stream().distinct().collect(Collectors.toList());
        int size = guests.size();
        int i0 = 0;
        while (i0 < size) {
            int i1 = Math.min(size, i0 + 100);
            guestRepository.insertBatch(segmentId, guests.subList(i0, i1));
            i0 = i1;
        }
    }

    @Override
    public List<Long> getCodesBySegmentId(UUID segmentId) {
        return guestRepository.getCodesBySegmentId(segmentId);
    }

    @Override
    public List<Long> getFrozenCodesBySegmentId(UUID segmentId) {
        return guestRepository.getFrozenCodesBySegmentId(segmentId);
    }

    @Override
    public List<Guest> getRefreshableGuestsBySegmentId(UUID segmentId) {
        Set<GuestCommunicationStatus> refreshableStatuses = Set.of(
                GuestCommunicationStatus.PENDING,
                GuestCommunicationStatus.SYSTEM_ERROR,
                GuestCommunicationStatus.DEFERRED);

        return guestRepository.findAllBySegmentId(segmentId)
                .stream()
                .filter(guest -> guest.getDeferredDate() == null ||
                        guest.getDeferredDate().compareTo(LocalDateTime.now()) < 0)
                .filter(guest -> guest.getCommunicationStatus() == null ||
                        refreshableStatuses.contains(guest.getCommunicationStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setFrozen(UUID segmentId, Map<String, Boolean> statuses) {
        statuses.entrySet().forEach(entry ->
                guestRepository.setFrozen(segmentId, Long.valueOf(entry.getKey()), entry.getValue()));
    }

    @Override
    @Transactional
    public void setUpcStatus(UUID segmentId, List<GuestDto> statuses) {
        statuses.forEach(entry ->
                guestRepository.setUpcStatus(segmentId, entry.getCode(),
                        entry.getCommunicationStatus(),
                        entry.getDeferredDate()));
    }
}
