package com.dede.ticketsystem.service;

public class PaymentResult {
    private boolean success;
    private boolean canRetry;
    private int remainingAttempts;
    private String message;
    private String redirect;

    public PaymentResult() {
    }

    public PaymentResult(boolean success, boolean canRetry, int remainingAttempts, String message, String redirect) {
        this.success = success;
        this.canRetry = canRetry;
        this.remainingAttempts = remainingAttempts;
        this.message = message;
        this.redirect = redirect;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
