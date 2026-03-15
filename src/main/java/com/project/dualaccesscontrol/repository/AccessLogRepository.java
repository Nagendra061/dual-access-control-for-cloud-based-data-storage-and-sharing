package com.project.dualaccesscontrol.repository;

import com.project.dualaccesscontrol.model.AccessLog;
import com.project.dualaccesscontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findByUserOrderByAccessedAtDesc(User user);
    List<AccessLog> findTop100ByOrderByAccessedAtDesc();
}
