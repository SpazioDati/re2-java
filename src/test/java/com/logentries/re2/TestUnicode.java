package com.logentries.re2;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
            Assert.assertEquals(cases.get(s), re.patchUnicodeWord(s));
        }
    }
}
