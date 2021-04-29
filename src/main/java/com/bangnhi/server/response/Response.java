package com.bangnhi.server.response;

public class Response<T> {
    private boolean ok;
    private String messenger;
    private T data;

    public Response(boolean ok, String messenger, T data) {
        this.ok = ok;
        this.messenger = messenger;
        this.data = data;
    }
}
