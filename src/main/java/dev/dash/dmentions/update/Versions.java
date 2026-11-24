package dev.dash.dmentions.update;

import java.util.ArrayList;
import java.util.List;

/** Compares version strings like "2.0.1" or "2.1.0-SNAPSHOT". */
public final class Versions {

    private Versions() {}

    /**
     * Whether {@code candidate} is a later version than {@code current}.
     * Numbers are compared one by one ("2.0.10" &gt; "2.0.9", "2.0" == "2.0.0").
     * With equal numbers, a version with a suffix ("2.0.0-SNAPSHOT") comes before the plain one ("2.0.0").
     */
    public static boolean isNewer(String candidate, String current) {
        return compare(candidate, current) > 0;
    }

    static int compare(String a, String b) {
        List<Integer> numbersA = numbers(a);
        List<Integer> numbersB = numbers(b);
        int length = Math.max(numbersA.size(), numbersB.size());
        for (int i = 0; i < length; i++) {
            int x = i < numbersA.size() ? numbersA.get(i) : 0;
            int y = i < numbersB.size() ? numbersB.get(i) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        // same numbers: the one without a suffix is the final release
        return Boolean.compare(!hasSuffix(a), !hasSuffix(b));
    }

    private static List<Integer> numbers(String version) {
        List<Integer> result = new ArrayList<>();
        for (String part : mainPart(version).split("\\.")) {
            String digits = part.replaceAll("\\D.*", ""); // "4b" -> "4"
            if (digits.isEmpty()) break;
            try {
                result.add(Integer.parseInt(digits));
            } catch (NumberFormatException e) {
                break;
            }
        }
        return result;
    }

    private static String mainPart(String version) {
        String trimmed = version.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) trimmed = trimmed.substring(1);
        int dash = trimmed.indexOf('-');
        return dash < 0 ? trimmed : trimmed.substring(0, dash);
    }

    private static boolean hasSuffix(String version) {
        return version.trim().indexOf('-') >= 0;
    }
}
