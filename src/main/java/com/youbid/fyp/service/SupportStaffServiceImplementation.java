package com.youbid.fyp.service;

import com.youbid.fyp.model.SupportStaff;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.SupportStaffRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportStaffServiceImplementation implements SupportStaffService {

    @Autowired
    private SupportStaffRepository supportStaffRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public SupportStaff createSupportStaff(Integer userId, String department) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));

        // Check if the user already has a support staff profile
        if (supportStaffRepository.findByUser(user).isPresent()) {
            throw new Exception("User already has a support staff profile");
        }

        // Check if the user has SUPPORT role
        if (!"SUPPORT".equals(user.getRole())) {
            throw new Exception("User must have SUPPORT role to be registered as support staff");
        }

        SupportStaff supportStaff = new SupportStaff(user, department);
        return supportStaffRepository.save(supportStaff);
    }

    @Override
    public SupportStaff updateSupportStaff(SupportStaff supportStaff, Integer supportStaffId) throws Exception {
        SupportStaff existingSupportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        if (supportStaff.getDepartment() != null) {
            existingSupportStaff.setDepartment(supportStaff.getDepartment());
        }

        if (supportStaff.getIsAvailable() != null) {
            existingSupportStaff.setIsAvailable(supportStaff.getIsAvailable());
        }

        return supportStaffRepository.save(existingSupportStaff);
    }

    @Override
    public void deleteSupportStaff(Integer supportStaffId) throws Exception {
        SupportStaff supportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        supportStaffRepository.delete(supportStaff);
    }

    @Override
    public SupportStaff findSupportStaffById(Integer supportStaffId) throws Exception {
        return supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));
    }

    @Override
    public SupportStaff findSupportStaffByUserId(Integer userId) throws Exception {
        return supportStaffRepository.findByUserId(userId)
                .orElseThrow(() -> new Exception("Support staff not found for user ID: " + userId));
    }

    @Override
    public List<SupportStaff> getAllSupportStaff() {
        return supportStaffRepository.findAll();
    }

    @Override
    public List<SupportStaff> getAvailableSupportStaff() {
        return supportStaffRepository.findAvailableSupportStaff();
    }

    @Override
    public List<SupportStaff> getAvailableSupportStaffByDepartment(String department) {
        return supportStaffRepository.findAvailableSupportStaffByDepartment(department);
    }

    @Override
    public SupportStaff assignChatToSupportStaff(Integer supportStaffId) throws Exception {
        SupportStaff supportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        supportStaff.incrementActiveChatsCount();
        supportStaff.setLastActiveTime(LocalDateTime.now());

        // If the support staff has reached the maximum number of active chats, set availability to false
        if (supportStaff.getActiveChatsCount() >= 5) { // Maximum of 5 concurrent chats
            supportStaff.setIsAvailable(false);
        }

        return supportStaffRepository.save(supportStaff);
    }

    @Override
    public SupportStaff finishChatForSupportStaff(Integer supportStaffId) throws Exception {
        SupportStaff supportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        supportStaff.decrementActiveChatsCount();
        supportStaff.setLastActiveTime(LocalDateTime.now());

        // If the support staff was unavailable due to maximum chats and now has capacity, set availability to true
        if (!supportStaff.getIsAvailable() && supportStaff.getActiveChatsCount() < 5) {
            supportStaff.setIsAvailable(true);
        }

        return supportStaffRepository.save(supportStaff);
    }

    @Override
    public SupportStaff updateAvailabilityStatus(Integer supportStaffId, boolean isAvailable) throws Exception {
        SupportStaff supportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        supportStaff.setIsAvailable(isAvailable);
        supportStaff.setLastActiveTime(LocalDateTime.now());

        return supportStaffRepository.save(supportStaff);
    }

}