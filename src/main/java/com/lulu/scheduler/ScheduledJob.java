package com.lulu.scheduler;

import com.lulu.service.JsonToCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

        Random random = new Random();

        // ✅ Create mutable map
        Map<String, Object> payload = new java.util.HashMap<>();

        payload.put("partnerId", "P" + random.nextInt(10000));
        payload.put("nuOrderNo", "NU-" + UUID.randomUUID().toString().substring(0, 8));
        payload.put("omnOrderNo", "OM-" + UUID.randomUUID().toString().substring(0, 8));
        payload.put("releaseNo", "R" + random.nextInt(10));
        payload.put("styleOption", randomStyle());
        payload.put("purchaseCycle", "Cycle-" + random.nextInt(5));
        payload.put("skuCount", random.nextInt(50) + 1);
        payload.put("season", randomSeason());

        log.info("Generated payload: {}", payload);

        service.upsertAndUpload(payload);
    }

    private String randomSeason() {
        String[] seasons = {"Spring", "Summer", "Autumn", "Winter"};
        return seasons[new Random().nextInt(seasons.length)];
    }

    private String randomStyle() {
        String[] styles = {"Red-XL", "Blue-M", "Green-L", "Black-S"};
        return styles[new Random().nextInt(styles.length)];
    }
}