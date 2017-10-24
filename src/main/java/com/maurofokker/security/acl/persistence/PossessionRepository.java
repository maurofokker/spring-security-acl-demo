package com.maurofokker.security.acl.persistence;

import com.maurofokker.security.acl.model.Possession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PossessionRepository extends JpaRepository<Possession, Long> {

    Possession findByName(String name);

}
