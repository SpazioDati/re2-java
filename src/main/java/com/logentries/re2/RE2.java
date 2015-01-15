/*
 *      Java Bindings for the RE2 Library
 *
 *      (c) 2012 Daniel Fiala <danfiala@ucw.cz>
 *
 */

package com.logentries.re2;

import com.logentries.re2.entity.CaptureGroup;
import com.logentries.re2.entity.NamedGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

public final class RE2 extends LibraryLoader implements AutoCloseable {
    private static native long compileImpl(final String pattern, final Options options) throws RegExprException;
    private static native void releaseImpl(final long pointer);
    private static native boolean fullMatchImpl(final String str, final long pointer, Object ... args);
    private static native boolean partialMatchImpl(final String str, final long pointer, Object ... args);
    private static native boolean fullMatchImpl(final String str, final String pattern, Object ... args);
    private static native boolean partialMatchImpl(final String str, final String pattern, Object ... args);
    private static native HashMap<Integer, String> getCaptureGroupNamesImpl(final long pointer);
    private static native int numberOfCapturingGroupsImpl(final long pointer);
    
    private long pointer;
    private boolean changedGroups = false;
    private Map<Integer, Integer> originalGroupMap = null;

    private void checkState() throws IllegalStateException {
        if (pointer == 0) {
            throw new IllegalStateException();
        }
    }
    boolean isClosed() {
        return pointer == 0;
    }

    public RE2(String pattern, final Options options) throws RegExprException {
        if (options.isUnicodeWord()) pattern = patchUnicodeWord(pattern);
        pointer = compileImpl(pattern, options);
        if (changedGroups) mapPatchedGroups();
    }
    public RE2(String pattern, final Options.Flag... options) throws RegExprException {
        Options opt = new Options();
        for (Options.Flag f : options) f.apply(opt);
        if (opt.isUnicodeWord()) pattern = patchUnicodeWord(pattern);
        pointer = compileImpl(pattern, opt);
        if (changedGroups) mapPatchedGroups();

    }

    ////////////////////////////////////////////////////////////////////////////////////
    /* Unicode word patch */
    static final int IDLE = 0, QUOTING = 2;
    static final String WORD_BOUNDARY_GNAME = "_ignore_";
    
    String patchUnicodeWord(String original) {
        StringBuilder buffer = new StringBuilder(original.length());
        int state = IDLE;
        int wordBoundaryCount = 0;
        for (int i=0; i<original.length(); i++) {
            char c = original.charAt(i);
            char next = 0; 
            if (i<original.length()-1) next = original.charAt(i+1);
            switch (state) {
                case IDLE:
                    if (c == '\\' && next > 0) {
                        if ( next == 'Q') {
                            buffer.append("\\Q");
                            state = QUOTING;
                        } else if ( next == 'w') {
                            buffer.append("[\\pL\\pN]");
                        } else if ( next == 'W') {
                            buffer.append("[^\\pL\\pN]");
                        } else if ( next == 'b') {
                            buffer.append("(?P<")
                                    .append(WORD_BOUNDARY_GNAME)
                                    .append(wordBoundaryCount)
                                    .append(">\\PL)");
                            wordBoundaryCount++;
                            changedGroups = true;
                        } else {
                            buffer.append(c).append(next);
                        }
                        i++;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case QUOTING:
                    if (c == '\\' && next == 'E') {
                        state = IDLE;
                        buffer.append("\\E");
                        i++;
                    } else 
                        buffer.append(c);
                    break;
            }
        }
        return buffer.toString();
    }
    private void mapPatchedGroups() {
        HashMap<Integer, String> groups = getCaptureGroupNameMap();
        int total = numberOfCapturingGroups();
        int offset = 0;
        int originalgroup = 1;
        
        originalGroupMap = new HashMap<>();
        
        for (int i = 1; i<total; i++){
            if (groups.containsKey(i) && groups.get(i).startsWith(WORD_BOUNDARY_GNAME)) {
                offset++;
            } else {
                originalGroupMap.put(originalgroup, originalgroup+offset);
                originalgroup++;
            }
        }
    }
    
    Map<Integer, Integer> getOriginalGroupMap() {
        return originalGroupMap;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////

    
    
    public static RE2 compile(final String pattern, final Options.Flag... options) {
        try {
            return new RE2(pattern, options);
        } catch (RegExprException ree) {
            throw new IllegalArgumentException(ree);
        }
    }

    public int numberOfCapturingGroups() {
        checkState();
        return numberOfCapturingGroupsImpl(pointer);
    }

    public HashMap<Integer, String> getCaptureGroupNameMap(){
        checkState();
        return getCaptureGroupNamesImpl(pointer);
    }
    
    public void dispoze() {
        if (pointer != 0) {
            releaseImpl(pointer);
            pointer = 0;
        }
    }

    public void close() {
        dispoze();
    }

    protected void finalize() throws Throwable {
        dispoze();
        super.finalize();
    }

    static private int checkArg(final Object obj) throws IllegalArgumentException {
        if (obj instanceof int[]) {
            return ((int[])obj).length;
        }
        if (obj instanceof long[]) {
            return ((long[])obj).length;
        }
        if (obj instanceof float[]) {
            return ((float[])obj).length;
        }
        if (obj instanceof double[]) {
            return ((double[])obj).length;
        }
        if (obj instanceof String[]) {
            return ((String[])obj).length;
        }
        throw new IllegalArgumentException();
    }

    static private void checkArgs(Object ... args) throws IllegalArgumentException {
        int length = 0;
        for (Object arg: args) {
            if ((length += checkArg(arg)) > 31) {
                throw new IllegalArgumentException("Only up to 31 arguments supported");
            }
        }
    }

    public static boolean fullMatch(final String str, final String pattern, Object ... args) {
        checkArgs(args);
        return fullMatchImpl(str, pattern, args);
    }

    public static boolean partialMatch(final String str, final String pattern, Object ... args) {
        checkArgs(args);
        return partialMatchImpl(str, pattern, args);
    }

    public boolean fullMatch(final String str, Object ... args) throws IllegalStateException {
        checkState();
        checkArgs(args);
        return fullMatchImpl(str, pointer, args);
    }

    public boolean partialMatch(final String str, Object ... args) throws IllegalStateException {
        checkState();
        checkArgs(args);
        return partialMatchImpl(str, pointer, args);
    }

    /**
     * This method returns ordered names.
     *
     * @param args
     * @return List of names for the capture groups
     * @throws IllegalStateException
     */
    public List<String> getCaptureGroupNames(Object... args) throws IllegalStateException {
        checkState();
        checkArgs(args);
        HashMap<Integer, String> nameMap = getCaptureGroupNamesImpl(pointer);
        return new ArrayList<>(nameMap.values());
    }
    

    public RE2Matcher matcher(final CharSequence str) {
        return matcher(str, true);
    }
    public RE2Matcher matcher(final CharSequence str, boolean fetchGroups) {
        checkState();
        if (changedGroups)
            return new RE2MatcherUnicodeWord(str, this, pointer);
        else
            return new RE2Matcher(str, this, pointer, fetchGroups);
    }
    public RE2Matcher matcher(final RE2String str) {
        return matcher(str, true);
    }
    public RE2Matcher matcher(final RE2String str, boolean fetchGroups) {
        checkState();
        if (changedGroups)
            return new RE2MatcherUnicodeWord(str, this, pointer);
        else
            return new RE2Matcher(str, this, pointer, fetchGroups);
    }

    /**
     * Gets the ordered capture groups for this event message and pattern.
     * @param str is an events message.
     * @return is a list of CaptureGroups.
     */
    public List<CaptureGroup> getCaptureGroups(final String str) {
        checkState();
        List<CaptureGroup> captureGroups = new ArrayList<>();
        RE2Matcher re2match = this.matcher(str);

        try {
            for (MatchResult match : re2match) {
                for (int i = 1; i < match.groupCount(); i++) {
                    if (match.start() > -1) {
                        captureGroups.add(new CaptureGroup(match.group(i), match.start(i), match.end(i)));
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return captureGroups;
        }
        return captureGroups;
    }

    /**
     * Returns a list of named capture groups and their position information in the event message.
     * @param names is a list of names to match against.
     * @param str is an events message.
     * @return is a list of named capture groups.
     */
    public List<NamedGroup> getNamedCaptureGroups(List<String> names, final String str) {
        List<NamedGroup> namedGroups = new ArrayList<>();
        List<CaptureGroup> captureGroups = getCaptureGroups(str);
        int len = names.size();

        if (len != captureGroups.size()) {
            // Matching text for a named group hasn't been found.
            return namedGroups;
        }

        for (int i = 0; i < len; i++) {
            if (captureGroups.get(i).start > -1) {
                namedGroups.add(new NamedGroup(names.get(i), captureGroups.get(i)));
            }
        }
        return namedGroups;
    }
}
