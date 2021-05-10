package com.bangnhi.note.data.response;

import lombok.Data;

@Data
public class BaseResponse<T> {
    Boolean ok;
    String message;
    T data = null;

    public BaseResponse(Boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public BaseResponse(Boolean ok, String message, T data) {
        this.ok = ok;
        this.message = message;
        this.data = data;
    }
}
