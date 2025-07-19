package com.landmark.hlss_java.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LuggageRequest {
    @NotBlank
    private String guestName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$")
    private String phone;

    @Min(1)
    private Integer luggageCount;
}