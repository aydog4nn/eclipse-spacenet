package org.dev.eclipse.util;

import java.security.SecureRandom;

public class CodeGeneratorUtil {

    private static final SecureRandom random = new SecureRandom();

    private static final String SAFE_ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789";

    public static String generateSafeToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Token length must be positive");
        }

        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(SAFE_ALPHANUMERIC.length());
            token.append(SAFE_ALPHANUMERIC.charAt(index));
        }

        return token.toString();
    }
}
