package com.youbid.fyp.controller;

import com.youbid.fyp.model.User;
import com.youbid.fyp.service.SupportStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/support")
public class PublicSupportController {

    @Autowired
    private SupportStaffService supportStaffService;

    @GetMapping("/departments")
    public ResponseEntity<?> getSupportDepartments() {
        try {
            // Get all available support staff
            List<String> departments = supportStaffService.getAllSupportStaff().stream()
                    .map(staff -> staff.getDepartment())
                    .distinct()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("departments", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> checkSupportAvailability(@RequestParam(required = false) String department) {
        try {
            boolean isAvailable;
            String estimatedWaitTime;
            List<String> availableDepartments;

            if (department != null && !department.isEmpty()) {
                // Check availability for specific department
                isAvailable = !supportStaffService.getAvailableSupportStaffByDepartment(department).isEmpty();
                estimatedWaitTime = isAvailable ? "less than 5 minutes" : "approximately 10-15 minutes";
                availableDepartments = Collections.singletonList(department);
            } else {
                // Check overall support availability
                List<String> departments = supportStaffService.getAvailableSupportStaff().stream()
                        .map(staff -> staff.getDepartment())
                        .distinct()
                        .collect(Collectors.toList());

                isAvailable = !departments.isEmpty();
                estimatedWaitTime = isAvailable ? "less than 5 minutes" : "approximately 10-15 minutes";
                availableDepartments = departments;
            }

            return ResponseEntity.ok(Map.of(
                    "isAvailable", isAvailable,
                    "estimatedWaitTime", estimatedWaitTime,
                    "availableDepartments", availableDepartments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}