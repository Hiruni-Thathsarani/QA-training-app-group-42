package utils;

public class TestUsers {
    public static final String ADMIN_USERNAME = envOrDefault("QA_ADMIN_USERNAME", "admin");
    public static final String ADMIN_PASSWORD = envOrDefault("QA_ADMIN_PASSWORD", "admin123");

    public static final String USER_USERNAME = envOrDefault("QA_USER_USERNAME", "testuser");
    public static final String USER_PASSWORD = envOrDefault("QA_USER_PASSWORD", "test123");

    private TestUsers() {}

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
