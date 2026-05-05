package com.calendario.agendarreservas.repository;

import com.calendario.agendarreservas.model.Role;
import com.calendario.agendarreservas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRut(String rut);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role ORDER BY u.apellidos ASC NULLS LAST, u.nombre ASC NULLS LAST")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles AND :excludeRole NOT MEMBER OF u.roles ORDER BY u.apellidos ASC NULLS LAST, u.nombre ASC NULLS LAST")
    List<User> findByRoleExcluding(@Param("role") Role role, @Param("excludeRole") Role excludeRole);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId);
}
