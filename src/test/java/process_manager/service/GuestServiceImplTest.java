package process_manager.service;

import process_manager.dto.GuestDto;
import process_manager.entity.Guest;
import process_manager.entity.GuestCommunicationStatus;
import process_manager.repository.GuestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(GuestServiceImpl.class)
public class GuestServiceImplTest {
    private static final UUID TARGET_SEGMENT_ID = UUID.randomUUID();

    private static final Long GUEST_1_CODE = 3L;
    private static final Long GUEST_2_CODE = 4L;
    private static final Long GUEST_3_CODE = 15L;
    private static final Long GUEST_4_CODE = 16L;
    private static final Long GUEST_5_CODE = 17L;


    @Autowired
    private GuestService guestService;

    @MockBean
    private GuestRepository guestRepository;

    @Captor
    private ArgumentCaptor<List<GuestDto>> captor;

    @Test
    void finalStatusFiltering() throws Exception {
        Guest guest1 = generateGuest(GUEST_1_CODE, TARGET_SEGMENT_ID);
        Guest guest2 = generateGuest(GUEST_2_CODE, TARGET_SEGMENT_ID);
        Guest guest3 = generateGuest(GUEST_3_CODE, TARGET_SEGMENT_ID);
        Guest guest4 = generateGuest(GUEST_4_CODE, TARGET_SEGMENT_ID);
        Guest guest5 = generateGuest(GUEST_5_CODE, TARGET_SEGMENT_ID);

        guest1.setCommunicationStatus(GuestCommunicationStatus.PENDING);
        guest2.setCommunicationStatus(GuestCommunicationStatus.DEFERRED);
        guest3.setCommunicationStatus(GuestCommunicationStatus.DELIVERED);  //final
        guest4.setCommunicationStatus(GuestCommunicationStatus.ERROR);      //final
        guest5.setCommunicationStatus(GuestCommunicationStatus.SYSTEM_ERROR);

        when(guestRepository.findAllBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(guest1, guest2, guest3, guest4, guest5));

        List<Guest> statuses =  guestService.getRefreshableGuestsBySegmentId(TARGET_SEGMENT_ID);
        assertEquals(3, statuses.size());
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_1_CODE)));
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_2_CODE)));
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_5_CODE)));
    }

    @Test
    void deferredFiltering() throws Exception {


        Guest guest1 = generateGuest(GUEST_1_CODE, TARGET_SEGMENT_ID);
        Guest guest2 = generateGuest(GUEST_2_CODE, TARGET_SEGMENT_ID);

        guest1.setCommunicationStatus(GuestCommunicationStatus.DEFERRED);
        guest2.setCommunicationStatus(GuestCommunicationStatus.DEFERRED);
        guest1.setDeferredDate(LocalDateTime.now().plusDays(3));
        guest2.setDeferredDate(LocalDateTime.now().minusDays(3));

        when(guestRepository.findAllBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(guest1, guest2));

        List<Guest> statuses =  guestService.getRefreshableGuestsBySegmentId(TARGET_SEGMENT_ID);
        assertEquals(1, statuses.size());
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_2_CODE)));
    }

    private Guest generateGuest(Long code, UUID segmentId) {
        Guest guest = new Guest();
        guest.setCode(code);
        guest.setId(code);
        guest.setSegmentId(segmentId);

        return guest;
    }
}
