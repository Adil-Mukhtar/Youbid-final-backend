package com.youbid.fyp.repository;

import com.youbid.fyp.model.SupportChat;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Integer> {

    List<SupportChat> findByUserOrderByLastActivityTimeDesc(User user);

    List<SupportChat> findBySupportAgentOrderByLastActivityTimeDesc(User supportAgent);

    @Query("SELECT c FROM SupportChat c WHERE (c.user = :user OR c.supportAgent = :user) ORDER BY c.lastActivityTime DESC")
    List<SupportChat> findUserSupportChats(@Param("user") User user);

    @Query("SELECT c FROM SupportChat c WHERE c.status = 'open' AND c.supportAgent IS NULL ORDER BY c.createdAt ASC")
    List<SupportChat> findUnassignedChats();

    @Query("SELECT c FROM SupportChat c WHERE c.status = 'open' AND c.supportAgent IS NULL AND c.department = :department ORDER BY c.createdAt ASC")
    List<SupportChat> findUnassignedChatsByDepartment(@Param("department") String department);

    @Query("SELECT c FROM SupportChat c WHERE c.status = :status ORDER BY c.lastActivityTime DESC")
    List<SupportChat> findChatsByStatus(@Param("status") String status);

    @Query("SELECT c FROM SupportChat c WHERE c.supportAgent = :supportAgent AND c.status = 'open' ORDER BY c.lastActivityTime DESC")
    List<SupportChat> findOpenChatsBySupportAgent(@Param("supportAgent") User supportAgent);

    @Query("SELECT c FROM SupportChat c WHERE (:query IS NULL OR LOWER(c.topic) LIKE %:query% OR LOWER(c.user.firstname) LIKE %:query% OR LOWER(c.user.lastname) LIKE %:query% OR LOWER(c.department) LIKE %:query%) ORDER BY c.lastActivityTime DESC")
    List<SupportChat> searchSupportChats(@Param("query") String query);
}