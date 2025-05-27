package com.tamnara.backend.news.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultiCategoryResponse {
    @JsonProperty("ALL")
    private NewsListResponse all;

    @JsonProperty("ECONOMY")
    private NewsListResponse economy;

    @JsonProperty("ENTERTAINMENT")
    private NewsListResponse entertainment;

    @JsonProperty("SPORTS")
    private NewsListResponse sports;

    @JsonProperty("KTB")
    private NewsListResponse ktb;
}
