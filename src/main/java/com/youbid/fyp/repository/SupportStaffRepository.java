package com.youbid.fyp.repository;

import com.youbid.fyp.model.SupportStaff;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportStaffRepository extends JpaRepository<SupportStaff, Integer> {
    Optional<SupportStaff> findByUser(User user);

    @Query("SELECT s FROM SupportStaff s WHERE s.isAvailable = true ORDER BY s.activeChatsCount ASC")
    List<SupportStaff> findAvailableSupportStaff();

    @Query("SELECT s FROM SupportStaff s WHERE s.department = :department AND s.isAvailable = true ORDER BY s.activeChatsCount ASC")
    List<SupportStaff> findAvailableSupportStaffByDepartment(@Param("department") String department);

    @Query("SELECT s FROM SupportStaff s WHERE s.user.id = :userId")
    Optional<SupportStaff> findByUserId(@Param("userId") Integer userId);
}