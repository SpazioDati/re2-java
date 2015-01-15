package com.logentries.re2;

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
}
