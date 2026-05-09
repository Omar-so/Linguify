package com.example.demo.service.AdminService;

import com.example.demo.Entitty.Images;
import com.example.demo.Entitty.Users;
import com.example.demo.Exceptions.InvalidPageException;
import com.example.demo.Exceptions.NoDataFoundException;
import com.example.demo.Exceptions.UserNotFoundException;
import com.example.demo.repoistery.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    AdminRepository adminRepository;

    // validate page and size before any query
    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new InvalidPageException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new InvalidPageException("Page size must be between 1 and 100");
        }
    }

    // all images + user + report
    public Page<Images> getAllImages(int page, int size) {
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = adminRepository.findAllImagesWithUserAndReport(pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No images found");
        }
        return result;
    }

    // all images WITH reports for a specific user (only images that have report)
    public Page<Images> getAllReportsByUser(Long userId, int page, int size) {
        validatePageRequest(page, size);
        if (!adminRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = adminRepository.findAllReportsByUser(userId, pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No reports found for user with id: " + userId);
        }
        return result;
    }

    // resolved reports only
    public Page<Images> getResolvedReports(int page, int size) {
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = adminRepository.findAllResolvedReports(pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No resolved reports found");
        }
        return result;
    }

    // unresolved reports only
    public Page<Images> getUnresolvedReports(int page, int size) {
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = adminRepository.findAllUnresolvedReports(pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No unresolved reports found");
        }
        return result;
    }

    // all images for a specific user
    public Page<Images> getAllImagesByUser(Long userId, int page, int size) {
        validatePageRequest(page, size);
        if (!adminRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = adminRepository.findAllImagesByUserId(userId, pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No images found for user with id: " + userId);
        }
        return result;
    }

    // all users with full history
    public Page<Users> getAllUsersWithHistory(int page, int size) {
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Users> result = adminRepository.findAllUsersWithHistory(pageable);
        if (result.isEmpty()) {
            throw new NoDataFoundException("No users found");
        }
        return result;
    }

    // delete user by id
    public void deleteUser(Long userId) {
        if (!adminRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        adminRepository.deleteUserById(userId);
    }
}