package com.tamnara.backend.news.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KtbResponse {
    @JsonProperty("KTB")
    private NewsListResponse ktb;
}
