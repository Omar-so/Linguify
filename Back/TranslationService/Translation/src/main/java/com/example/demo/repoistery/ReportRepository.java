// repository/ReportRepository.java
package com.example.demo.repoistery;

import com.example.demo.Entitty.Reports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Reports, Long> {

    // find report by image id
    Optional<Reports> findByImageId(Long imageId);

    // find report by id and user id (security check)
    @Query("SELECT r FROM Reports r JOIN r.image i JOIN i.user u WHERE r.id = :reportId AND u.id = :userId")
    Optional<Reports> findByIdAndUserId(
            @Param("reportId") Long reportId,
            @Param("userId") Long userId);

    // get all reports for logged in user
    @Query("SELECT r FROM Reports r JOIN FETCH r.image i JOIN FETCH i.user u WHERE u.id = :userId")
    Page<Reports> findAllByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    // update description
    @Modifying
    @Transactional
    @Query("UPDATE Reports r SET r.description = :description WHERE r.id = :reportId AND r.image.user.id = :userId")
    int updateDescription(
            @Param("reportId") Long reportId,
            @Param("description") String description,
            @Param("userId") Long userId);

    // delete report
    @Modifying
    @Transactional
    @Query("DELETE FROM Reports r WHERE r.id = :reportId AND r.image.user.id = :userId")
    int deleteByIdAndUserId(
            @Param("reportId") Long reportId,
            @Param("userId") Long userId);
}