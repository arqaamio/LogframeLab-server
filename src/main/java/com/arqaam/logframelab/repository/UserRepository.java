package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
