package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.entity.Guest;
import com.x5.bigdata.dvcm.process.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class GuestServiceImpl implements GuestService {
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public void save(UUID segmentId, List<Long> guests) {
        guests.forEach(code -> guestRepository.save(new Guest(code, segmentId)));
    }

    @Override
    public List<Long> getCodesBySegmentId(UUID segmentId) {
        return guestRepository.getCodesBySegmentId(segmentId);
    }

    @Override
    @Transactional
    public void setFrozen(UUID segmentId, Map<String, Boolean> statuses) {
        statuses.entrySet().forEach(entry ->
                guestRepository.setFrozen(segmentId, Long.valueOf(entry.getKey()), entry.getValue()));
    }

    @Override
    @Transactional
    public void setUpcStatus(UUID segmentId, Map<String, String> statuses) {
        statuses.entrySet().forEach(entry ->
                guestRepository.setUpcStatus(segmentId, Long.valueOf(entry.getKey()), entry.getValue()));
    }
}
