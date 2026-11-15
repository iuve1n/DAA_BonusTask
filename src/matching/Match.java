package matching;

public class Match {
    private final int patternIndex;
    private final int startIndex;

    public Match(int patternIndex, int startIndex) {
        this.patternIndex = patternIndex;
        this.startIndex = startIndex;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }
}
