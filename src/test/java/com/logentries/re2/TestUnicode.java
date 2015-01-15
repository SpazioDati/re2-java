package com.logentries.re2;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by edt on 1/15/15.
 */
public class TestUnicode {

    @Test
    public void testReplace ( ) {
        Map<String, String> cases = new HashMap<String, String>() {{
            put("", "");
            put("\\b", "(?P<_ignore_0>\\PL)");
            put("\\w", "[\\pL\\pN]");
            put("\\W", "[^\\pL\\pN]");
            put("\\b\\b\\b", "(?P<_ignore_0>\\PL)(?P<_ignore_1>\\PL)(?P<_ignore_2>\\PL)");
            put("\\w{3}\\b", "[\\pL\\pN]{3}(?P<_ignore_0>\\PL)");
        }};

        for ( String s : cases.keySet() ) {
            RE2 re = RE2.compile(s);
            assertEquals(cases.get(s), re.patchUnicodeWord(s));
        }
    }

    @Test
    public void testIgnoreMiddle() throws Exception {
        RE2 regex = new RE2("(\\w+)\\basd\\b(\\w+)", Options.UNICODE_WORD);
        RE2Matcher matcher = regex.matcher("éé asd éé");
        assertTrue(matcher.findNext());
        assertEquals("éé asd éé", matcher.group());
        assertEquals("éé", matcher.group(1));
        assertEquals("éé", matcher.group(2));

        matcher = regex.matcher("ééasdéé");
        assertFalse(matcher.findNext());
    }

    public void testIgnoreEnd() throws Exception {
        RE2 regex = new RE2("((\\w+)\\b)", Options.UNICODE_WORD);
        RE2Matcher matcher = regex.matcher("éaéa");
        assertTrue(matcher.findNext());
        assertEquals("éaéa", matcher.group());
    }
}
