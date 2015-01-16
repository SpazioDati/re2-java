package com.logentries.re2;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by edt on 1/15/15.
 */
public class TestUnicode {

    @Ignore
    @Test
    public void testReplace ( ) {
        Map<String, String> cases = new HashMap<String, String>() {{
            put("", "");
            put("\\b", RE2.BOUNDARY_REPLACE);
            put("\\w", RE2.WORD_REPLACE);
            put("\\W", RE2.NON_WORD_REPLACE);
            put("\\b\\b", RE2.BOUNDARY_REPLACE + RE2.BOUNDARY_REPLACE);
        }};

        for ( String s : cases.keySet() ) {
            RE2 re = RE2.compile(s);
            assertEquals(cases.get(s), re.patchUnicodeWord(s));
        }
    }

    @Test
    public void testSimpleUnicode () throws Exception {
        RE2 regex = new RE2("\\w+", Options.UNICODE_WORD);

        Map<String,String[]> cases = new HashMap<String, String[]>() {{
            put("pio pio pio", new String[]{"pio", "pio", "pio"});
            put("pio pio perché é ᴘrêché",
                    new String[]{"pio", "pio", "perché", "é", "ᴘrêché"});
            put("='.12é ᴘrêc2hé///pupp2a perché1 ",
                    new String[]{"12é", "ᴘrêc2hé", "pupp2a", "perché1"});
        }};


        for ( String s : cases.keySet() ) {
            RE2Matcher matcher = regex.matcher(s);
            String[] matches = cases.get(s);

            for (String match : matches) {
                assertTrue(matcher.findNext());
                assertEquals(match, matcher.group());
            }
        }
    }

    public static class TestWordBoundaries {
        @Test
        public void testIgnoreMiddle() throws Exception {
            RE2 regex = new RE2("(\\b\\w+) asd (\\w+\\b)", Options.UNICODE_WORD);
            RE2Matcher matcher = regex.matcher("a éé asd éé ");
            assertTrue(matcher.findNext());

            assertEquals("éé asd éé", matcher.group());
            assertEquals("éé", matcher.group(1));
            assertEquals("éé", matcher.group(2));

            matcher = regex.matcher("ééasdéé");
            assertFalse(matcher.find());
        }

        @Test
        public void testIgnoreEnd() throws Exception {
            RE2 regex = new RE2("((\\w+)\\b)", Options.UNICODE_WORD);
            RE2Matcher matcher = regex.matcher("éaéa");
            assertTrue(matcher.findNext());
            assertEquals("éaéa", matcher.group());

            matcher = regex.matcher("éaéa.[',");
            assertTrue(matcher.findNext());
            assertEquals("éaéa", matcher.group());
        }

        @Test
        public void testIgnoreBeginning() throws Exception {
            RE2 regex = new RE2("(\\b\\w+)", Options.UNICODE_WORD);
            RE2Matcher matcher = regex.matcher("éaéa");
            assertTrue(matcher.findNext());
            assertEquals("éaéa", matcher.group());

            matcher = regex.matcher("/,/.,é1é");
            assertTrue(matcher.findNext());
            assertEquals("é1é", matcher.group());
        }

        @Test
        public void moreTests () throws Exception {
            RE2 regex = new RE2("\\b\\w+\\b", Options.UNICODE_WORD);

            Map<String,String[]> cases = new HashMap<String, String[]>() {{
                put("pio pio pio", new String[]{"pio", "pio", "pio"});
                put("pio pio perché é ᴘrêché", new String[]{"pio", "pio", "perché", "é", "ᴘrêché"});
                put("='.12é ᴘrêc2hé///pupp2a perché1 ", new String[]{"12é", "ᴘrêc2hé", "pupp2a", "perché1"});
                put(" d1sé=... ᴘrêc2hé ", new String[]{"d1sé", "ᴘrêc2hé"});
            }};


            for ( String s : cases.keySet() ) {
                RE2Matcher matcher = regex.matcher(s);
                String[] matches = cases.get(s);

                for (String match : matches) {
                    assertTrue(matcher.findNext());
                    assertEquals(match, matcher.group());
                }
            }
        }
    }
}
