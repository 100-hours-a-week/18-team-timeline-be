package com.tamnara.backend.alarm.repository;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByTargetTypeAndTargetIdOrderByIdDesc(AlarmType targetType, Long targetId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Alarm a
        WHERE a.createdAt < :cutoff
    """)
    void deleteAllOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
