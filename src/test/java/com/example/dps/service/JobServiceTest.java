package com.example.dps.service;

import com.example.dps.dto.JobDTO;
import com.example.dps.repository.JobLogRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private JobLogRepo jobLogRepo;

    @InjectMocks
    private JobLogService jobLogService;

    @Test
    void updateLogs_success() {
            // Given
            JobDTO jobDTO = new JobDTO();
            jobDTO.setRunAt(LocalDateTime.parse("2024-06-01T10:00:00"));
            jobDTO.setProductsUpdated(100);
            jobDTO.setStatus("SUCCESS");

            // When
            jobLogService.updateLogs(jobDTO);

            // Then
            // Verify that the repository's save method was called with the expected JobLog object
            verify(jobLogRepo).save(any());
    }
}
