package com.escpos.printer.authentication;

public class SessionManager {
    private String accessToken;
    private String refreshToken;

    private SessionManager() {
        // login user with .env credentials??
    }

    private static class Holder {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    // must be static to be accessed globally
    public static SessionManager getSession() { return Holder.INSTANCE; }

    // Use ´synchronized´ because multiple threads (printing / polling)
    // might check the token at once
    public synchronized void updateSession(String access, String refresh) {
        accessToken = access;
        refreshToken = refresh;
    }

    public synchronized String getAccessToken() { return accessToken; }
    public synchronized String getRefreshToken() { return refreshToken; }
}
