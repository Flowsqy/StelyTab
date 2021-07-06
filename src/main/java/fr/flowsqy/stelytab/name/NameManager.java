package fr.flowsqy.stelytab.name;

import fr.flowsqy.stelytab.io.NameLoader;

import java.util.HashMap;
import java.util.List;

public class NameManager {

    // Prefix pattern : GroupsChars (Fix) PlayerInsidePriorityChars (Variable)

    private static void getAllPrefixes(List<String> prefixes, String current, int length, int limit) {
        for (int i = 32; i < 127; i++) {
            if (prefixes.size() >= limit)
                return;
            final String prefix = current + (char) i;
            if (prefix.length() < length)
                getAllPrefixes(prefixes, prefix, length, limit);
            else
                prefixes.add(prefix);
        }
    }

    private static int getPrefixLength(int prefixesCount) {
        int prefixLength = 0;
        int j = 1;
        for (int i = 1; i < prefixesCount; i++) {
            if ((j *= 95) >= prefixesCount) {
                prefixLength = i;
                break;
            }
        }
        return prefixLength;
    }

    public void refresh() {

    }

    public void setup(HashMap<String, NameLoader.PrioritizedName> nameByGroupName) {

    }

}
