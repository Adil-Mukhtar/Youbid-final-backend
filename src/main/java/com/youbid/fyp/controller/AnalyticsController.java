package com.youbid.fyp.controller;

import com.youbid.fyp.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable Integer userId) {
        try {
            Map<String, Object> analytics = analyticsService.getUserRevenueAnalytics(userId);
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overall")
    public ResponseEntity<Map<String, Object>> getOverallAnalytics() {
        try {
            Map<String, Object> analytics = analyticsService.getOverallAnalytics();
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}