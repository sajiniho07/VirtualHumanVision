package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.Text;

public interface TextRepository extends JpaRepository<Text, Long> {
}