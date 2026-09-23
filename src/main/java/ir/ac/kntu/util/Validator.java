package ir.ac.kntu.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0|98|\\+98)\\d{10}$");

    private static final Pattern MEMBER_ID_PATTERN =
            Pattern.compile("^(STU|FAC|GST)\\d{6}$");

    private static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("^(BOK|MAG|EBK|AUD)-?\\d{7,8}$");

    private static final Pattern ISBN_PATTERN =
            Pattern.compile("^(978|979)\\d{10}$");

    private static final Pattern ISSN_PATTERN =
            Pattern.compile("^\\d{4}-\\d{3}[\\dXx]$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+[{]};:'\",<.>/?~`|\\\\\\s])[A-Za-z\\d!@#$%^&*()\\-_=+[{]};:'\",<.>/?~`|\\\\\\s]{6,}$");

    private Validator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidMemberId(String id) {
        return id != null && MEMBER_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidItemId(String id) {
        return id != null && ITEM_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidISBN(String isbn) {
        return isbn != null && ISBN_PATTERN.matcher(isbn).matches();
    }

    public static boolean isValidISSN(String issn) {
        return issn != null && ISSN_PATTERN.matcher(issn).matches();
    }

    public static boolean isValidPublishYear(int year) {
        return year >= 1000 && year <= 2026;
    }

    public static boolean isValidURL(String url) {
        return url != null && !url.trim().isEmpty() && url.startsWith("https://");
    }

    public static boolean isStrongPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}