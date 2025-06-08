package com.tamnara.backend.alarm.repository;

import com.tamnara.backend.alarm.domain.UserAlarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {
    Page<UserAlarm> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Boolean existsByIdAndIsCheckedTrue(Long userAlarmId);

    @Modifying
    @Query("""
        UPDATE UserAlarm ua
        SET ua.isChecked = true, ua.checkedAt = :checkedAt
        WHERE ua.id = :userAlarmId
    """)
    void checkUserAlarm(@Param("userAlarmId") Long userAlarmId, @Param("checkedAt") LocalDateTime checkedAt);
}
