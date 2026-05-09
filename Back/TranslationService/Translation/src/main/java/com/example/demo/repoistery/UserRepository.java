// repository/UserRepository.java
package com.example.demo.repoistery;

import com.example.demo.Entitty.Images;
import com.example.demo.Entitty.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>
{

}