package com.example.dps.service;

import com.example.dps.dto.JobDTO;
import com.example.dps.entity.JobLog;
import com.example.dps.repository.JobLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobLogService {
    private final JobLogRepo repo;
    @Autowired
    public JobLogService(JobLogRepo repo) {
        this.repo = repo;
    }

    public void updateLogs(JobDTO jobLog){
        JobLog log = new JobLog();
        log.setRunAt(jobLog.getRunAt());
        log.setProductsUpdated(jobLog.getProductsUpdated());
        log.setJobStatus(jobLog.getStatus());

        repo.save(log);

    }
}
