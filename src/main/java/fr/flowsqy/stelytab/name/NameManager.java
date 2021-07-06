package fr.flowsqy.stelytab.name;

import fr.flowsqy.stelytab.io.NameLoader;

import java.util.HashMap;
import java.util.List;

public class NameManager {

    // Prefix pattern : GroupsChars (Fix) PlayerInsidePriorityChars (Variable)

    private static void getAllPrefixes(List<String> prefixes, String current, int length, int limit) {
        for (int charIndex = 32 /* Minimum index char */; charIndex < 127 /* Maximum index char */ ; charIndex++) {
            if (prefixes.size() >= limit)
                return;
            final String prefix = current + (char) charIndex;
            if (prefix.length() < length)
                getAllPrefixes(prefixes, prefix, length, limit);
            else
                prefixes.add(prefix);
        }
    }

    private static int getPrefixLength(int prefixesNumber) {
        for (int charCount = 1, prefixesCount = 1; charCount < 5 /* Max tries */; charCount++, prefixesCount *= 95 /* Possible chars */) {
            if (prefixesCount >= prefixesNumber) {
                return charCount;
            }
        }
        return -1;
    }

    public void refresh() {

    }

    public void setup(HashMap<String, NameLoader.PrioritizedName> nameByGroupName) {

    }

}
