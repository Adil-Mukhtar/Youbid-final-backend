package com.youbid.fyp.service;

import com.youbid.fyp.model.SupportStaff;
import com.youbid.fyp.model.User;

import java.util.List;

public interface SupportStaffService {
    SupportStaff createSupportStaff(Integer userId, String department) throws Exception;
    SupportStaff updateSupportStaff(SupportStaff supportStaff, Integer supportStaffId) throws Exception;
    void deleteSupportStaff(Integer supportStaffId) throws Exception;
    SupportStaff findSupportStaffById(Integer supportStaffId) throws Exception;
    SupportStaff findSupportStaffByUserId(Integer userId) throws Exception;
    List<SupportStaff> getAllSupportStaff();
    List<SupportStaff> getAvailableSupportStaff();
    List<SupportStaff> getAvailableSupportStaffByDepartment(String department);
    SupportStaff assignChatToSupportStaff(Integer supportStaffId) throws Exception;
    SupportStaff finishChatForSupportStaff(Integer supportStaffId) throws Exception;
    SupportStaff updateAvailabilityStatus(Integer supportStaffId, boolean isAvailable) throws Exception;
}