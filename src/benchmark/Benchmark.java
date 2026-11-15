package benchmark;

import matching.AhoCorasick;
import matching.KmpMatcher;
import matching.Match;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    private static final int[] PATTERN_COUNTS = {10, 100, 1000};
    private static final int[] TEXT_LENGTHS = {1000, 10000, 100000};
    private static final int WARMUP_RUNS = 5;
    private static final int MEASURED_RUNS = 5;
    private static final long SEED = 20261115L;
    private static final String ALPHABET = "abcd";
    private static volatile long resultSink;

    private static class Timing {
        private long buildNanos;
        private long searchNanos;
        private int matchCount;
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "results/benchmark.csv" : args[0]);
        List<String> lines = new ArrayList<>();
        lines.add("patterns,text_length,algorithm,average_build_ns,average_search_ns,average_total_ns,matches");
        for (int patternCount : PATTERN_COUNTS) {
            for (int textLength : TEXT_LENGTHS) {
                Random random = new Random(SEED + patternCount * 1000003L + textLength);
                String text = randomString(random, textLength);
                List<String> patterns = createPatterns(random, patternCount, text);
                verifyMatches(patterns, text);
                for (int run = 0; run < WARMUP_RUNS; run++) {
                    measureAho(patterns, text);
                    measureKmp(patterns, text);
                }
                Timing aho = new Timing();
                Timing kmp = new Timing();
                for (int run = 0; run < MEASURED_RUNS; run++) {
                    if (run % 2 == 0) {
                        add(aho, measureAho(patterns, text));
                        add(kmp, measureKmp(patterns, text));
                    } else {
                        add(kmp, measureKmp(patterns, text));
                        add(aho, measureAho(patterns, text));
                    }
                }
                lines.add(toCsv(patternCount, textLength, "AhoCorasick", aho));
                lines.add(toCsv(patternCount, textLength, "KMP", kmp));
            }
        }
        Path parent = output.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Files.write(output, lines);
        System.out.println("Saved 18 benchmark rows to " + output);
    }

    private static List<String> createPatterns(Random random, int count, String text) {
        List<String> patterns = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int length = 4 + random.nextInt(9);
            if (i % 2 == 0) {
                int start = random.nextInt(text.length() - length + 1);
                patterns.add(text.substring(start, start + length));
            } else {
                patterns.add(randomString(random, length));
            }
        }
        return patterns;
    }

    private static String randomString(Random random, int length) {
        StringBuilder text = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            text.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return text.toString();
    }

    private static Timing measureAho(List<String> patterns, String text) {
        long start = System.nanoTime();
        AhoCorasick matcher = new AhoCorasick(patterns);
        long built = System.nanoTime();
        List<Match> matches = matcher.search(text);
        long finished = System.nanoTime();
        return timing(start, built, finished, matches.size());
    }

    private static Timing measureKmp(List<String> patterns, String text) {
        long start = System.nanoTime();
        KmpMatcher matcher = new KmpMatcher(patterns);
        long built = System.nanoTime();
        List<Match> matches = matcher.search(text);
        long finished = System.nanoTime();
        return timing(start, built, finished, matches.size());
    }

    private static Timing timing(long start, long built, long finished, int matches) {
        Timing result = new Timing();
        result.buildNanos = built - start;
        result.searchNanos = finished - built;
        result.matchCount = matches;
        resultSink += matches;
        return result;
    }

    private static void add(Timing total, Timing run) {
        total.buildNanos += run.buildNanos;
        total.searchNanos += run.searchNanos;
        total.matchCount = run.matchCount;
    }

    private static String toCsv(int patterns, int textLength, String algorithm, Timing timing) {
        return String.format(Locale.ROOT, "%d,%d,%s,%.1f,%.1f,%.1f,%d",
                patterns, textLength, algorithm,
                timing.buildNanos / (double) MEASURED_RUNS,
                timing.searchNanos / (double) MEASURED_RUNS,
                (timing.buildNanos + timing.searchNanos) / (double) MEASURED_RUNS,
                timing.matchCount);
    }

    private static void verifyMatches(List<String> patterns, String text) {
        long[] aho = encode(new AhoCorasick(patterns).search(text));
        long[] kmp = encode(new KmpMatcher(patterns).search(text));
        if (!Arrays.equals(aho, kmp)) {
            throw new IllegalStateException("Matchers returned different occurrences");
        }
    }

    private static long[] encode(List<Match> matches) {
        long[] values = new long[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            values[i] = ((long) match.getPatternIndex() << 32) | (match.getStartIndex() & 0xffffffffL);
        }
        Arrays.sort(values);
        return values;
    }
}
