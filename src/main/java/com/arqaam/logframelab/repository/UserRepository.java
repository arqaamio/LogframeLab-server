package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  @Query(value = "from User u join u.groupMembership m join m.group g where g.name = :groupName")
  List<User> findFirstUserByGroupName(@Param("groupName") String groupName);

  Optional<User> findByUsername(String username);

  @Query(value = "from User u join fetch u.groupMembership m where u.username = :username")
  Optional<User> findByUsernameWithGroupMemberships(@Param("username") String username);

  boolean existsByUsername(String username);

  @Query(value = "from User u join u.groupMembership m join m.group order by u.username")
  List<User> getAllUsersWithTheirGroups();
}
