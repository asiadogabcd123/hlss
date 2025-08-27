package com.landmark.hlss_java.dto;

import lombok.Data;
import java.util.List;

@Data
public class RetrieveRequest {
    private List<String> scannedLuggage;
    private String idCardNumber;
}