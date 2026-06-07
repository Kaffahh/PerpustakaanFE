import java.util.prefs.Preferences;

public final class AuthSessionStore {
    private static final Preferences PREFS = Preferences.userRoot().node("library-hub-auth");
    private static final String KEY_SESSION_TOKEN = "sessionToken";

    private AuthSessionStore() {}

    public static void saveSessionToken(String token) {
        if (token == null || token.isBlank()) {
            clear();
            return;
        }
        PREFS.put(KEY_SESSION_TOKEN, token.trim());
    }

    public static String getSessionToken() {
        String token = PREFS.get(KEY_SESSION_TOKEN, "");
        return token == null || token.isBlank() ? null : token.trim();
    }

    public static void clear() {
        PREFS.remove(KEY_SESSION_TOKEN);
    }
}