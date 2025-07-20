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
import java.util.Optional;

@Repository
public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {
    Page<UserAlarm> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Optional<UserAlarm> findFirstByUserIdOrderByIdDesc(Long userId);

    @Query("""
        SELECT ua FROM UserAlarm ua
        JOIN ua.alarm a
        JOIN Bookmark b ON b.news.id = a.targetId
        WHERE ua.user.id = :userId
          AND b.user.id = :userId
          AND a.targetType = com.tamnara.backend.alarm.domain.AlarmType.NEWS
        ORDER BY ua.id DESC
    """)
    Page<UserAlarm> findBookmarkAlarms(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE UserAlarm ua
        SET ua.isChecked = true, ua.checkedAt = :checkedAt
        WHERE ua.id = :userAlarmId
    """)
    void checkUserAlarm(@Param("userAlarmId") Long userAlarmId, @Param("checkedAt") LocalDateTime checkedAt);

    boolean existsByUserIdAndIsCheckedFalse(Long userId);
}
