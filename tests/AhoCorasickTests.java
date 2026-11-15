import matching.AhoCorasick;
import matching.Match;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AhoCorasickTests {
    public static void main(String[] args) {
        checkMatches(Arrays.asList("he", "she", "his", "hers"), "ushers");
        checkMatches(Arrays.asList("a", "aa", "aaa"), "aaaaa");
        checkMatches(Arrays.asList("ab", "b", "abc", "bc"), "abcxab");
        checkMatches(Arrays.asList("cat", "cat", "dog"), "cat dog cat");
        checkMatches(Arrays.asList("longpattern", "absent"), "abc");
        checkMatches(Arrays.asList("a"), "");
        checkMatches(Collections.emptyList(), "anything");
        testRepeatedSearch();
        testInvalidPatterns();
        testRandomInputs();
        System.out.println("Aho-Corasick tests passed");
    }

    private static void checkMatches(List<String> patterns, String text) {
        List<String> expected = new ArrayList<>();
        for (int p = 0; p < patterns.size(); p++) {
            String pattern = patterns.get(p);
            for (int start = 0; start <= text.length() - pattern.length(); start++) {
                if (text.startsWith(pattern, start)) {
                    expected.add(p + ":" + start);
                }
            }
        }
        List<String> actual = encode(new AhoCorasick(patterns).search(text));
        Collections.sort(expected);
        if (!actual.equals(expected)) {
            throw new AssertionError("Wrong matches for text: " + text
                    + "; expected " + expected + ", got " + actual);
        }
    }

    private static List<String> encode(List<Match> matches) {
        List<String> values = new ArrayList<>();
        for (Match match : matches) {
            values.add(match.getPatternIndex() + ":" + match.getStartIndex());
        }
        Collections.sort(values);
        return values;
    }

    private static void testRepeatedSearch() {
        List<String> patterns = new ArrayList<>(Arrays.asList("aba"));
        AhoCorasick matcher = new AhoCorasick(patterns);
        patterns.set(0, "changed");
        if (!encode(matcher.search("ababa")).equals(Arrays.asList("0:0", "0:2"))
                || !matcher.search("none").isEmpty()
                || !encode(matcher.search("aba")).equals(Arrays.asList("0:0"))) {
            throw new AssertionError("Searches must be independent of previous calls and input list changes");
        }
    }

    private static void testInvalidPatterns() {
        for (String pattern : new String[]{"", null}) {
            try {
                new AhoCorasick(Arrays.asList(pattern));
                throw new AssertionError("Invalid pattern was accepted");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    private static void testRandomInputs() {
        Random random = new Random(42);
        for (int trial = 0; trial < 200; trial++) {
            List<String> patterns = new ArrayList<>();
            for (int p = 0; p < 10; p++) {
                patterns.add(randomString(random, 1 + random.nextInt(8)));
            }
            checkMatches(patterns, randomString(random, 100));
        }
    }

    private static String randomString(Random random, int length) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < length; i++) {
            value.append("abc".charAt(random.nextInt(3)));
        }
        return value.toString();
    }
}
