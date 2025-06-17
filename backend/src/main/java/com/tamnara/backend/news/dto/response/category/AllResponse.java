package com.tamnara.backend.news.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllResponse {
    @JsonProperty("ALL")
    private NewsListResponse all;
}
