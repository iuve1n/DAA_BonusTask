import matching.AhoCorasick;
import matching.KmpMatcher;
import matching.Match;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MatcherComparisonTests {
    public static void main(String[] args) {
        checkMatches(Arrays.asList("he", "she", "his", "hers"), "ushers");
        checkMatches(Arrays.asList("aba", "ba", "a", "aba"), "ababa");
        checkMatches(Arrays.asList("a", "aa", "aaa"), "aaaaa");
        checkMatches(Arrays.asList("start", "middle", "end"), "start middle end");
        checkMatches(Arrays.asList("absent", "longpattern"), "abc");
        checkMatches(Arrays.asList("a"), "");
        checkMatches(Collections.emptyList(), "abc");
        testRepeatedSearch();
        testInvalidInputs();
        Random random = new Random(123);
        for (int trial = 0; trial < 200; trial++) {
            List<String> patterns = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                patterns.add(randomString(random, 1 + random.nextInt(10)));
            }
            checkMatches(patterns, randomString(random, 100));
        }
        System.out.println("Matcher comparison tests passed");
    }

    private static void checkMatches(List<String> patterns, String text) {
        List<String> expected = new ArrayList<>();
        for (int p = 0; p < patterns.size(); p++) {
            for (int start = 0; start <= text.length() - patterns.get(p).length(); start++) {
                if (text.startsWith(patterns.get(p), start)) {
                    expected.add(p + ":" + start);
                }
            }
        }
        Collections.sort(expected);
        List<String> aho = encode(new AhoCorasick(patterns).search(text));
        List<String> kmp = encode(new KmpMatcher(patterns).search(text));
        if (!expected.equals(aho) || !expected.equals(kmp)) {
            throw new AssertionError("Matchers disagree with expected matches for: " + text);
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
        KmpMatcher matcher = new KmpMatcher(patterns);
        patterns.set(0, "changed");
        if (!encode(matcher.search("ababa")).equals(Arrays.asList("0:0", "0:2"))
                || !matcher.search("none").isEmpty()
                || !encode(matcher.search("aba")).equals(Arrays.asList("0:0"))) {
            throw new AssertionError("KMP should support independent repeated searches");
        }
    }

    private static void testInvalidInputs() {
        for (String pattern : new String[]{"", null}) {
            try {
                new KmpMatcher(Arrays.asList(pattern));
                throw new AssertionError("Invalid pattern was accepted");
            } catch (IllegalArgumentException expected) {
            }
        }
        try {
            new KmpMatcher(null);
            throw new AssertionError("Null pattern list was accepted");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new KmpMatcher(Arrays.asList("a")).search(null);
            throw new AssertionError("Null text was accepted");
        } catch (IllegalArgumentException expected) {
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
