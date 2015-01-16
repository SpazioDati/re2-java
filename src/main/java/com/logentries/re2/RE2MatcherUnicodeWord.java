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
        if ( !result ) return false;

        /*Set<Integer> ignore = regex.getIgnoreGropus();
        for ( int i = 0; i < groupCount(); ++i ) {
            if ( !ignore.contains(i) )
                System.out.println("Good:   '" + group(i) + "'");
            else
                System.out.println("Ignored: '" + group(i) + "'");
        }*/

        groups = patchGroups(groups, regex.getIgnoreGropus());

        return true;
    }

    static ArrayList<Range> patchGroups(ArrayList<Range> groups, Set<Integer> ignoreRanges) {
        ArrayList<Range> patched = new ArrayList<>();
        Set<Integer> startRanges = new HashSet<>();

        // remove the groups to ignore
        for ( int i = 0; i < groups.size(); ++i ) {
            if ( !ignoreRanges.contains(i) )
                patched.add(groups.get(i));
            else {
                Range ri = groups.get(i);
                if ( ri.start >= 0 && ri.start != ri.end ) {
                    //System.out.println(ri.start);
                    startRanges.add(ri.start);
                }
            }

        }

        // adjust the ranges of the remaining ones
        for ( int i = 0; i < patched.size(); ++i ) {
            Range ri = patched.get(i);

            if ( ri.start != ri.end && ri.start >= 0 ) {

                if (startRanges.contains(ri.start))
                    patched.set(i, Range.of(ri.start + 1, ri.end));

                // eventually I just modified the range!
                ri = patched.get(i);

                if (startRanges.contains(ri.end - 1))
                    patched.set(i, Range.of(ri.start, ri.end - 1));
            }
        }

        return patched;
    }
}
