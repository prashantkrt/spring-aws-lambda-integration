package com.lulu.service;

import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

public interface S3Service {
    public void uploadCsv(String csvContent);

    public List<S3Object> listFiles(String prefix);

    public boolean fileExists(String key);

    public String readFile(String key);

}
