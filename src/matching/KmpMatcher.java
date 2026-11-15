package matching;

import java.util.ArrayList;
import java.util.List;

public class KmpMatcher {
    private final List<String> patterns;
    private final List<int[]> prefixTables = new ArrayList<>();

    public KmpMatcher(List<String> patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("Pattern list cannot be null");
        }
        this.patterns = new ArrayList<>(patterns);
        for (String pattern : this.patterns) {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-empty strings");
            }
            prefixTables.add(buildPrefixTable(pattern));
        }
    }

    private int[] buildPrefixTable(String pattern) {
        int[] table = new int[pattern.length()];
        int prefixLength = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (prefixLength > 0 && pattern.charAt(i) != pattern.charAt(prefixLength)) {
                prefixLength = table[prefixLength - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(prefixLength)) {
                prefixLength++;
            }
            table[i] = prefixLength;
        }
        return table;
    }

    public List<Match> search(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        List<Match> matches = new ArrayList<>();
        for (int patternIndex = 0; patternIndex < patterns.size(); patternIndex++) {
            String pattern = patterns.get(patternIndex);
            int[] table = prefixTables.get(patternIndex);
            int matchedLength = 0;
            for (int position = 0; position < text.length(); position++) {
                char character = text.charAt(position);
                while (matchedLength > 0 && character != pattern.charAt(matchedLength)) {
                    matchedLength = table[matchedLength - 1];
                }
                if (character == pattern.charAt(matchedLength)) {
                    matchedLength++;
                }
                if (matchedLength == pattern.length()) {
                    matches.add(new Match(patternIndex, position - pattern.length() + 1));
                    matchedLength = table[matchedLength - 1];
                }
            }
        }
        return matches;
    }
}
