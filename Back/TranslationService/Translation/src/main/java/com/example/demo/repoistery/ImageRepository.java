// repository/ImageRepository.java
package com.example.demo.repoistery;

import com.example.demo.Entitty.Images;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

public interface ImageRepository extends JpaRepository<Images, Long> {

    // find image by id and user id (security check)
    @Query("SELECT i FROM Images i JOIN FETCH i.user u WHERE i.id = :imageId AND u.id = :userId")
    java.util.Optional<Images> findByIdAndUserId(
            @Param("imageId") Long imageId,
            @Param("userId") Long userId);

    // delete specific image by id and userId
    @Modifying
    @Transactional
    @Query("DELETE FROM Images i WHERE i.id = :imageId AND i.user.id = :userId")
    int deleteImageByIdAndUserId(
            @Param("imageId") Long imageId,
            @Param("userId") Long userId);

    // get all images + reports for logged in user
    @Query("SELECT i FROM Images i JOIN FETCH i.user u LEFT JOIN FETCH i.report r WHERE u.id = :userId")
    Page<Images> findAllHistoryByUserId(
            @Param("userId") Long userId,
            Pageable pageable);
}