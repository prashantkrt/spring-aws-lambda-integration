package com.lulu.scheduler;

import com.lulu.service.JsonToCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ScheduledJob {

    private final JsonToCsvService service;

    public ScheduledJob(JsonToCsvService service) {
        this.service = service;
    }

    @EventListener
    public void handleScheduleEvent(Object event) {

        log.info("Cron triggered: {}", event);

        Map<String, Object> payload = Map.of(
                "partnerId", "P1001",
                "nuOrderNo", "NU12345",
                "omnOrderNo", "OM56789",
                "releaseNo", "R1",
                "styleOption", "Blue-XL",
                "purchaseCycle", "Spring2026",
                "skuCount", 10,
                "season", "Summer"
        );

        service.upsertAndUpload(payload);
    }
}