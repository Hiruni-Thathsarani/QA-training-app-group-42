package utils;

public class Urls {

    // ================= BASE =================
    public static final String BASE_URL = "http://localhost:8080";

    // ================= UI =================
    public static final String UI_LOGIN = BASE_URL + "/ui/login";
    public static final String UI_DASHBOARD = BASE_URL + "/ui/dashboard";
    public static final String UI_SELL_NEW = BASE_URL + "/ui/sales/new";

    // ================= AUTH (Swagger) =================
    // Swagger shows: POST /api/auth/login
    public static final String API_LOGIN = BASE_URL + "/api/auth/login";

    // ================= CATEGORY =================
    // Swagger shows: POST /api/categories (admin only)
    public static final String API_CATEGORIES = BASE_URL + "/api/categories";
    public static final String API_CATEGORIES_PAGE = BASE_URL + "/api/categories/page";
    public static final String API_CATEGORIES_MAIN = BASE_URL + "/api/categories/main";
    public static final String API_CATEGORIES_SUB = BASE_URL + "/api/categories/sub-categories";
    public static final String API_CATEGORIES_SUMMARY = BASE_URL + "/api/categories/summary";

    // ================= PLANTS =================
    public static final String API_PLANTS = BASE_URL + "/api/plants";

    // ================= SALES =================
    public static final String API_SALES = BASE_URL + "/api/sales";

    public static final String UI_CATEGORIES = BASE_URL + "/ui/categories";
    public static final String UI_CATEGORIES_ADD = BASE_URL + "/ui/categories/add";
    public static final String UI_PLANTS = BASE_URL + "/ui/plants";
    public static final String UI_SALES = BASE_URL + "/ui/sales";

    private Urls() {
    }
}
