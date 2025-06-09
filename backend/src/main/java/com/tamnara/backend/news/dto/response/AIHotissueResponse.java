package com.tamnara.backend.news.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AIHotissueResponse {
    private List<String> keywords;
}
