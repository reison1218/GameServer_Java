package game.utils;

import com.google.common.base.CharMatcher;

/** Created by wyt on 16-12-5. */
public class Paths {

    private static final CharMatcher fileSepCharMatcher = CharMatcher.is('/');

    public static String join(String a, String b) {
        a = fileSepCharMatcher.trimTrailingFrom(a);
        b = fileSepCharMatcher.trimLeadingFrom(b);

        String r;
        if (a.isEmpty()) {
            r = b;
        } else {
            r = a + "/" + b;
        }
        return normalize(r);
    }

    public static String normalize(String s) {
        return fileSepCharMatcher.trimTrailingFrom(s);
    }
}
