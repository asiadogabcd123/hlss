package com.landmark.hlss_java.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "luggage_photos")
public class LuggagePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "luggage_id", nullable = false)
    private Luggage luggage;

    @Column(nullable = false, length = 20)
    private String photoType; // checkin/damage等

    @Column(nullable = false, length = 255)
    private String photoUrl;
}