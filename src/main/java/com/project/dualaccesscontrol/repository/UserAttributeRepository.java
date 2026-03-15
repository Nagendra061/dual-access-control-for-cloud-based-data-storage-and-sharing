package com.project.dualaccesscontrol.repository;

import com.project.dualaccesscontrol.model.User;
import com.project.dualaccesscontrol.model.UserAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserAttributeRepository extends JpaRepository<UserAttribute, Long> {
    List<UserAttribute> findByUser(User user);
}
