package com.sbi.branchdarpan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.User;
import com.sbi.branchdarpan.model.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPfid(String pfid);

    List<User> findByRoleIn(List<Role> roles);
}
