package com.Auton.GIBGMain.Response;

public class ResponseWrapper<T> {
private String message;
private T data;

public ResponseWrapper(String message, T data) {
        this.message = message;
        this.data = data;
        }

public String getMessage() {
        return message;
        }

public T getData() {
        return data;
        }

        // Other getters and setters if needed
        }
