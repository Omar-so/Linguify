package com.example.demo.repoistery;
import com.example.demo.Entitty.Users;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
@Repository
public interface AuthRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);

}
