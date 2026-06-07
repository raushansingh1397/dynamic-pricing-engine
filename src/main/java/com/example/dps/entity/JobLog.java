package com.example.dps.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jobId;
    private LocalDateTime runAt;
    private String jobStatus;
    private Integer productsUpdated;
}
