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
    Optional<User> findByZaloUserId(String zaloUserId);

    /**
     * Searches for users based on a keyword against multiple fields.
     * @param keyword The term to search for.
     * @return A list of matching {@link User}s.
     */
    @Query("SELECT u FROM User u WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.productionLine) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<User> search(@Param("keyword") String keyword);


    /**
     * Retrieves a unique, non-empty list of all departments from the User table.
     * The `DISTINCT` keyword ensures that each department name appears only once in the result.
     * @return A list of unique department names.
     */
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL AND u.department <> ''")
    List<String> findDistinctDepartments();

    /**
     * Retrieves a unique, non-empty list of all production lines from the User table.
     * @return A list of unique production line names.
     */
    @Query("SELECT DISTINCT u.productionLine FROM User u WHERE u.productionLine IS NOT NULL AND u.productionLine <> ''")
    List<String> findDistinctProductionLines();


}