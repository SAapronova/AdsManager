package com.x5.bigdata.dvcm.process.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GuestService {

    void save(UUID segmentId, List<Long> guests);

    List<Long> getCodesBySegmentId(UUID segmentId);

    List<Long> getFrozenCodesBySegmentId(UUID segmentId);

    void setFrozen(UUID segmentId, Map<String, Boolean> statuses);

    void setUpcStatus(UUID segmentId, Map<String, String> statuses);
}
