package com.project.dualaccesscontrol.repository;

import com.project.dualaccesscontrol.model.FileEntity;
import com.project.dualaccesscontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByOwnerAndIsDeletedFalse(User owner);
    List<FileEntity> findByIsDeletedFalse();
}
