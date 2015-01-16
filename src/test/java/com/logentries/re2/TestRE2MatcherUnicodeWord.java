package com.logentries.re2;

import org.junit.Test;

import java.util.*;

import com.logentries.re2.RE2Matcher.Range;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by edt on 1/15/15.
 */
public class TestRE2MatcherUnicodeWord {


    @Test
    public void testPatch ( ) {
        ArrayList<Range> input = new ArrayList<Range>() {{
            add(Range.of(0,10));
            add(Range.of(10,16));
            add(Range.of(10,11));
            add(Range.of(20,23));
            add(Range.of(22,23));
        }};
        Set<Integer> ignore = new HashSet<Integer>() {{
            add(2);
            add(4);
        }};

        ArrayList<Range> output = RE2MatcherUnicodeWord.patchGroups(input, ignore);
        assertEquals(3, output.size());
        assertEquals(input.get(0), output.get(0));
        // removed one at the beginning
        assertEquals(11, output.get(1).start);
        assertEquals(16, output.get(1).end);
        // removed one at the end
        assertEquals(20, output.get(2).start);
        assertEquals(22, output.get(2).end);
    }
}
