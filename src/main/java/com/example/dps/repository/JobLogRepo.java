package com.example.dps.repository;

import com.example.dps.entity.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogRepo extends JpaRepository<JobLog, Integer> {
}
