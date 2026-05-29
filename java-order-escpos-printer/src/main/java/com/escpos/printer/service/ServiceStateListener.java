package com.escpos.printer.service;

public interface ServiceStateListener {
    void onStateChanged(String state, boolean isError);
    void onLogMessage(String message);
    void onCriticalFailure();
}
