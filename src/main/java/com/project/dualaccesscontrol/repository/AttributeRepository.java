package com.project.dualaccesscontrol.repository;

import com.project.dualaccesscontrol.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByAttributeName(String attributeName);
}
