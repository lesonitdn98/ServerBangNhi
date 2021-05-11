package com.bangnhi.note.data.response;

import lombok.Data;

@Data
public class BaseResponse<T> {
    Boolean ok;
    String message;
    T data = null;
    int status;

    public BaseResponse(Boolean ok, String message, int status) {
        this.ok = ok;
        this.message = message;
        this.status = status;
    }

    public BaseResponse(Boolean ok, String message, T data, int status) {
        this.ok = ok;
        this.message = message;
        this.data = data;
        this.status = status;
    }
}
