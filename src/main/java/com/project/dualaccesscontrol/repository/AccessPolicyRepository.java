package com.project.dualaccesscontrol.repository;

import com.project.dualaccesscontrol.model.AccessPolicy;
import com.project.dualaccesscontrol.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccessPolicyRepository extends JpaRepository<AccessPolicy, Long> {
    List<AccessPolicy> findByFileAndIsActiveTrue(FileEntity file);
    List<AccessPolicy> findByFile(FileEntity file);
}
