package com.youbid.fyp.response;

public class ApiResponse {

    private String message;
    private boolean status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ApiResponse() {
        //
    }

    public ApiResponse(String message, boolean status) {
        super();
        this.message = message;
        this.status = status;
    }
}