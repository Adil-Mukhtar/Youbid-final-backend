package com.youbid.fyp.controller;

import com.youbid.fyp.model.SupportStaff;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.SupportStaffService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support-staff")
public class SupportStaffController {

    @Autowired
    private SupportStaffService supportStaffService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSupportStaff(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            String department = (String) request.get("department");

            SupportStaff supportStaff = supportStaffService.createSupportStaff(userId, department);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Support staff created successfully",
                            "supportStaff", supportStaff
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> updateSupportStaff(
            @PathVariable Integer id,
            @RequestBody SupportStaff supportStaff,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);

            // Check if the user is a support staff or admin
            if (!"ADMIN".equals(currentUser.getRole())) {
                SupportStaff currentSupportStaff = supportStaffService.findSupportStaffByUserId(currentUser.getId());

                // Support staff can only update their own profile
                if (!currentSupportStaff.getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only update your own profile"));
                }
            }

            SupportStaff updatedSupportStaff = supportStaffService.updateSupportStaff(supportStaff, id);

            return ResponseEntity.ok(updatedSupportStaff);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSupportStaff(@PathVariable Integer id) {
        try {
            supportStaffService.deleteSupportStaff(id);

            return ResponseEntity.ok(Map.of("message", "Support staff deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getSupportStaffById(@PathVariable Integer id) {
        try {
            SupportStaff supportStaff = supportStaffService.findSupportStaffById(id);

            return ResponseEntity.ok(supportStaff);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getSupportStaffByUserId(@PathVariable Integer userId) {
        try {
            SupportStaff supportStaff = supportStaffService.findSupportStaffByUserId(userId);

            return ResponseEntity.ok(supportStaff);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportStaff>> getAllSupportStaff() {
        List<SupportStaff> supportStaffList = supportStaffService.getAllSupportStaff();

        return ResponseEntity.ok(supportStaffList);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportStaff>> getAvailableSupportStaff() {
        List<SupportStaff> supportStaffList = supportStaffService.getAvailableSupportStaff();

        return ResponseEntity.ok(supportStaffList);
    }

    @GetMapping("/available/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportStaff>> getAvailableSupportStaffByDepartment(@PathVariable String department) {
        List<SupportStaff> supportStaffList = supportStaffService.getAvailableSupportStaffByDepartment(department);

        return ResponseEntity.ok(supportStaffList);
    }

    @PutMapping("/availability/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            Boolean isAvailable = request.get("isAvailable");

            if (isAvailable == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "isAvailable field is required"));
            }

            // Check if the user is a support staff or admin
            if (!"ADMIN".equals(currentUser.getRole())) {
                SupportStaff currentSupportStaff = supportStaffService.findSupportStaffByUserId(currentUser.getId());

                // Support staff can only update their own availability
                if (!currentSupportStaff.getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only update your own availability"));
                }
            }

            SupportStaff updatedSupportStaff = supportStaffService.updateAvailabilityStatus(id, isAvailable);

            return ResponseEntity.ok(updatedSupportStaff);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<?> getCurrentSupportStaffProfile(@RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            SupportStaff supportStaff = supportStaffService.findSupportStaffByUserId(currentUser.getId());

            return ResponseEntity.ok(supportStaff);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}