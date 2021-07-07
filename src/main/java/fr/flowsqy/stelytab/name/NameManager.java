package fr.flowsqy.stelytab.name;

import fr.flowsqy.stelytab.io.NameLoader;

import java.util.Arrays;
import java.util.HashMap;

public class NameManager {

    // Prefix pattern : GroupsChars (Fix) PlayerInsidePriorityChars (Variable)

    private HashMap<Integer, String[]> groupsForPriority;
    private HashMap<String, Name> nameForGroup;

    private static String[] getAllPrefixes(byte length, int limit) {
        if (length < 1 || limit < 1)
            return new String[0];
        final byte MIN_CHAR = 32;
        final String[] prefixes = new String[limit];
        final byte[] current = new byte[length--];
        Arrays.fill(current, MIN_CHAR);
        byte charCount = 0;
        int prefixesCount = 0;
        while (limit > prefixesCount) {
            if (current[charCount] == Byte.MAX_VALUE) {
                current[charCount] = MIN_CHAR;
                current[--charCount]++;
            } else if (charCount < length) {
                charCount++;
            } else {
                prefixes[prefixesCount++] = new String(current);
                current[charCount]++;
            }
        }
        return prefixes;
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
