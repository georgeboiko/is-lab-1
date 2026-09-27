package itmo.is.lab1.api.security;

public final class BearerTokens {

    private static final String PREFIX = "Bearer ";

    private BearerTokens() {}

    public static String fromHeader(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                        true, 0, PREFIX, 0, PREFIX.length())) {
            return null;
        }
        String token = authorizationHeader.substring(PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
