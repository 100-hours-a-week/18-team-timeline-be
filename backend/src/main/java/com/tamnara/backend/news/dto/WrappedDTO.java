package com.tamnara.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WrappedDTO<T> {
    private boolean success;
    private String message;
    private T data;
}