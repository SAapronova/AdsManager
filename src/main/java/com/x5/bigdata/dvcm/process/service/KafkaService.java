package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignStatusDto;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaService {
    private final boolean kafkaEnable;
    private final int sendTimeout;
    private final String statusTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaService(@Value("${kafka.producer.send.timeout.sec}") int sendTimeout,
                        @Value("${kafka.topic.campaign.status}") String statusTopic,
                        @Value("${kafka.enable:false}") Boolean kafkaEnable,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaEnable = kafkaEnable;
        this.sendTimeout = sendTimeout;
        this.kafkaTemplate = kafkaTemplate;
        this.statusTopic = statusTopic;
    }

    @SuppressWarnings("java:S2142")
    public void sendProcessStatus(String campaignCode, CampaignStatus status) {
        log.info("Init send campaign {} status {} to kafka {} ", campaignCode, status, statusTopic);
        if (kafkaEnable) {
            try {
                CampaignStatusDto statusDto = CampaignStatusDto.builder()
                        .campaignCode(campaignCode)
                        .status(status)
                        .statusDate(LocalDateTime.now())
                        .build();
                kafkaTemplate.send(statusTopic, statusDto).get(sendTimeout, TimeUnit.SECONDS);
                log.info("Send campaign status to kafka {} {}", statusTopic, statusDto);
            } catch (Exception e) {
                log.error("Exception in send campaign {} status {} to kafka {} {}", campaignCode, status, statusTopic, e);
            }
        }
    }
}
