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

public interface AdminRepository extends JpaRepository<Images, Long> {

    // all images with user and report
    @Query("SELECT i FROM Images i JOIN FETCH i.user u LEFT JOIN FETCH i.report r")
    Page<Images> findAllImagesWithUserAndReport(Pageable pageable);

    // resolved reports
    @Query("SELECT i FROM Images i JOIN FETCH i.user u JOIN FETCH i.report r WHERE r.resolved = true")
    Page<Images> findAllResolvedReports(Pageable pageable);

    // unresolved reports
    @Query("SELECT i FROM Images i JOIN FETCH i.user u JOIN FETCH i.report r WHERE r.resolved = false")
    Page<Images> findAllUnresolvedReports(Pageable pageable);

    // all reports by specific user
    @Query("SELECT i FROM Images i JOIN FETCH i.user u JOIN FETCH i.report r WHERE u.id = :userId")
    Page<Images> findAllReportsByUser(@Param("userId") Long userId, Pageable pageable);

    // NEW — all images by user id (with or without report) paginated
    @Query("SELECT i FROM Images i JOIN FETCH i.user u LEFT JOIN FETCH i.report r WHERE u.id = :userId")
    Page<Images> findAllImagesByUserId(@Param("userId") Long userId, Pageable pageable);

    // NEW — all users with full history (images + reports)
    @Query("SELECT DISTINCT u FROM Users u LEFT JOIN FETCH u.images i LEFT JOIN FETCH i.report r")
    Page<Users> findAllUsersWithHistory(Pageable pageable);

    // NEW — delete user by id
    @Modifying
    @Transactional
    @Query("DELETE FROM Users u WHERE u.id = :userId")
    void deleteUserById(@Param("userId") Long userId);
}