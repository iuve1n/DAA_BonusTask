package matching;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AhoCorasick {
    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private final List<Integer> patternIndices = new ArrayList<>();
        private Node failure;
        private Node output;
    }

    private final Node root = new Node();
    private final List<String> patterns;

    public AhoCorasick(List<String> patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("Pattern list cannot be null");
        }
        this.patterns = new ArrayList<>(patterns);
        for (int index = 0; index < this.patterns.size(); index++) {
            String pattern = this.patterns.get(index);
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-empty strings");
            }
            addPattern(pattern, index);
        }
        buildFailureLinks();
    }

    private void addPattern(String pattern, int index) {
        Node current = root;
        for (int i = 0; i < pattern.length(); i++) {
            char character = pattern.charAt(i);
            Node child = current.children.get(character);
            if (child == null) {
                child = new Node();
                current.children.put(character, child);
            }
            current = child;
        }
        current.patternIndices.add(index);
    }

    private void buildFailureLinks() {
        root.failure = root;
        Queue<Node> queue = new ArrayDeque<>();
        for (Node child : root.children.values()) {
            child.failure = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char character = entry.getKey();
                Node child = entry.getValue();
                Node fallback = current.failure;
                while (fallback != root && !fallback.children.containsKey(character)) {
                    fallback = fallback.failure;
                }
                Node next = fallback.children.get(character);
                child.failure = next == null ? root : next;
                if (!child.failure.patternIndices.isEmpty()) {
                    child.output = child.failure;
                } else {
                    child.output = child.failure.output;
                }
                queue.add(child);
            }
        }
    }

    public List<Match> search(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        List<Match> matches = new ArrayList<>();
        Node current = root;
        for (int position = 0; position < text.length(); position++) {
            char character = text.charAt(position);
            while (current != root && !current.children.containsKey(character)) {
                current = current.failure;
            }
            Node next = current.children.get(character);
            current = next == null ? root : next;
            Node output = current;
            while (output != null) {
                for (int patternIndex : output.patternIndices) {
                    int start = position - patterns.get(patternIndex).length() + 1;
                    matches.add(new Match(patternIndex, start));
                }
                output = output.output;
            }
        }
        return matches;
    }
}
