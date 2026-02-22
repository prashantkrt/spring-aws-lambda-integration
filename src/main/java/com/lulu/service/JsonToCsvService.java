package com.lulu.service;

import java.util.List;
import java.util.Map;


public interface JsonToCsvService {
    public void processAndUpload(List<Map<String, Object>> data);
    public void upsertAndUpload(Map<String, Object> record);
}
