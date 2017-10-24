package com.maurofokker.security.acl.persistence;

import com.maurofokker.security.acl.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
