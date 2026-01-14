package com.selling.repository;

import com.selling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findUserById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAllByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'user' OR u.role = 'USER'")
    long customerCount();

}
