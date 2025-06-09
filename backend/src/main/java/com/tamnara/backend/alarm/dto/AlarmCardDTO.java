package com.tamnara.backend.alarm.dto;

import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.news.util.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AlarmCardDTO {
    private Long id;

    @Length(max = 14, message = "알림 제목은 14자까지만 가능합니다.")
    private String title;

    @Length(max = 255, message = "알림 내용은 255자까지만 가능합니다.")
    private String content;

    private Boolean isChecked;
    private LocalDateTime checkedAt;

    @ValueOfEnum(enumClass = AlarmType.class, message = "타겟 종류 값이 올바르지 않습니다.")
    private String targetType;
    private Long targetId;
}
