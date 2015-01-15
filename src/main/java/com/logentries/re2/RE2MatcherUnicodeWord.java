package com.logentries.re2;

import java.util.*;

/**
 * Created by edt on 1/15/15.
 */
public class RE2MatcherUnicodeWord extends RE2Matcher {

    RE2MatcherUnicodeWord(CharSequence input, RE2 regex, long re2Pointer) {
        this(input, regex, re2Pointer, true);
    }

    RE2MatcherUnicodeWord(CharSequence input, RE2 regex, long re2Pointer, boolean fetchGroups) {
        super(input, regex, re2Pointer, fetchGroups);
    }

    public boolean find(int start, int end) {
        boolean result = super.find(start, end);
        groups = patchGroups(groups, regex.getIgnoreGropus());

        return result;
    }

    static ArrayList<Range> patchGroups(ArrayList<Range> groups, Set<Integer> ignoreRanges) {
        ArrayList<Range> patched = new ArrayList<>();

        // remove the groups to ignore
        for ( int i = 0; i < groups.size(); ++i ) {
            if ( !ignoreRanges.contains(i) )
                patched.add(groups.get(i));
        }

        // adjust the ranges of the remaining ones
        for ( int i = 0; i < patched.size(); ++i ) {
            for ( int j : ignoreRanges ) {
                Range ri = patched.get(i);

                // the match if exist is one character
                if ( groups.get(j).start == ri.start )
                    patched.set(i, Range.of(ri.start + 1, ri.end));
                if ( groups.get(j).end == ri.end )
                    patched.set(i, Range.of(ri.start, ri.end - 1));
            }
        }

        return patched;
    }
}
