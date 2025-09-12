package com.gsm.repository;

import com.gsm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.productionLine) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<User> search(@Param("keyword") String keyword);

    Optional<User> findByZaloUserId(String zaloUserId);

    // YÊU CẦU MỚI: Lấy danh sách Department duy nhất
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL AND u.department <> ''")
    List<String> findDistinctDepartments();

    // YÊU CẦU MỚI: Lấy danh sách Production Line duy nhất
    @Query("SELECT DISTINCT u.productionLine FROM User u WHERE u.productionLine IS NOT NULL AND u.productionLine <> ''")
    List<String> findDistinctProductionLines();

}