package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.VhvObject;

public interface ObjectRepository extends JpaRepository<VhvObject, Long> {
}
