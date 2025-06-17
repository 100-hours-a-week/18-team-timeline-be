package com.tamnara.backend.alarm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlarmTest {

    @Test
    void 알림_기본값_초기화_검증() {
        // given & when
        Alarm alarm = new Alarm();

        // then
        assertNull(alarm.getId());
        assertNull(alarm.getTitle());
        assertNull(alarm.getContent());
        assertNull(alarm.getCreatedAt());
        assertNull(alarm.getTargetType());
        assertNull(alarm.getTargetId());
    }

    @Test
    void 알림_제목과_내용과_타겟종류와_타겟ID_설정_검증() {
        // given
        Alarm alarm = new Alarm();

        // when
        alarm.setTitle("제목");
        alarm.setContent("내용");
        alarm.setTargetType(AlarmType.NEWS);
        alarm.setTargetId(1L);

        // then
        assertEquals("제목", alarm.getTitle());
        assertEquals("내용", alarm.getContent());
        assertEquals(AlarmType.NEWS, alarm.getTargetType());
        assertEquals(1L, alarm.getTargetId());
    }

    @Test
    void ID가_동일하면_동일한_알림_검증() {
        // given
        Alarm alarm1 = new Alarm();
        Alarm alarm2 = new Alarm();

        // when
        alarm1.setId(1L);
        alarm2.setId(1L);

        // then
        assertEquals(alarm1, alarm2);
    }

    @Test
    void 알림_종류의_전체_정의값_검증() {
        // given & when
        AlarmType[] values = AlarmType.values();

        // then
        assertThat(values).containsExactlyInAnyOrder(
                AlarmType.NEWS,
                AlarmType.POLLS
        );
    }

    @Test
    void 문자열로부터_enum_변환_검증() {
        // given
        String input1 = "NEWS";
        String input2 = "POLLS";

        // when
        AlarmType result1 = AlarmType.valueOf(input1);
        AlarmType result2 = AlarmType.valueOf(input2);

        // then
        assertEquals(AlarmType.NEWS, result1);
        assertEquals(AlarmType.POLLS, result2);
    }

    @Test
    void 잘못된_문자열이_enum_변환에_실패_검증() {
        // given
        String invalidInput = "INVALID";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            AlarmType.valueOf(invalidInput);
        });
    }
}
