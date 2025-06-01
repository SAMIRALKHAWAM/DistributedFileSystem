package clientapp;

import commonlib.models.Token;

public class SessionManager {
    private static Token token;

    public static void setToken(Token newToken) {
        token = newToken;
    }

    public static Token getToken() {
        return token;
    }

    public static boolean isAuthenticated() {
        return token != null;
    }

    public static void clear() {
        token = null;
    }
}
