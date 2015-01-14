package com.logentries.re2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

public class RE2Matcher implements MatchResult, AutoCloseable, Iterable<MatchResult> {

    private static native boolean findImpl(
        final Object matcher,
        final long re2_pointer,
        final long str_pointer,
        final int fetch_groups,
        final int start,
        final int end
    );

    static class Range {
        int start, end;
        static Range of(int start, int end) {
            Range r = new Range();
            r.start = start;
            r.end = end;
            return r;
        }
    }

    public static void addGroup(RE2Matcher obj, int start, int end) {
        if (start >= 0 && end >= 0) {
            start = obj.utf8input.charPos(start);
            end = obj.utf8input.charPos(end);
        }
        obj.groups.add(Range.of(start, end));
    }


    private ArrayList<Range> groups;
    protected RE2String utf8input;
    protected RE2String managedString;
    protected long re2Pointer = 0;
    protected RE2 regex;
    private boolean matched;
    private boolean fetchGroups;
    private boolean unicodeWord;

    RE2Matcher(RE2String input, RE2 regex, long re2Pointer, boolean fetchGroups, boolean unicodeWord) {
        this.utf8input = input;
        this.matched = false;
        this.groups = new ArrayList<>(fetchGroups? regex.numberOfCapturingGroups() + 1 : 1);
        this.re2Pointer = re2Pointer;
        this.regex = regex; //to avoid that re2Pointer could be garbaged
        this.fetchGroups = fetchGroups;
        this.managedString = null;
        this.unicodeWord = unicodeWord;
    }


    RE2Matcher(CharSequence input, RE2 regex, long re2Pointer, boolean fetchGroups, boolean unicodeWord) {
        this(new RE2String(input), regex, re2Pointer, fetchGroups, unicodeWord);
        this.managedString = utf8input;
    }
    public void close() {
        if (managedString != null)
            managedString.close();
    }



    public boolean found() {
        return matched;
    }

    public boolean findNext() {
        if (!matched) return find();
        else return find(end(0));
    }

    public boolean find() {
        return find(0);
    }
    public boolean find(int start) {
        return find(start, utf8input.length());
    }

    public boolean find(int start, int end) {
        groups.clear();
        matched = false;

        if (utf8input.isClosed()) throw new IllegalStateException("String buffer has been already closed");
        if (regex.isClosed()) throw new IllegalStateException("Regex has been already closed");

        start = utf8input.bytePos(start);
        end = utf8input.bytePos(end);
        int ngroups = fetchGroups ? regex.numberOfCapturingGroups() + 1 : 1;
        @SuppressWarnings("deprecation")
        long stringPointer = utf8input.pointer();
        return matched = findImpl(this, re2Pointer, stringPointer, ngroups, start, end);
    }

    private void checkGroup(int group) {
        if (!matched) throw new IllegalStateException("The pattern has not been matched!");
        if (group >= groups.size()) throw new IllegalStateException("Group n. "+group+" is not in pattern!");
    }

    @Override
    public int start() {
        return start(0);
    }

    @Override
    public int start(int group) {
        checkGroup(group);
        return groups.get(group).start;
    }

    @Override
    public int end() {
        return end(0);
    }

    @Override
    public int end(int group) {
        checkGroup(group);
        return groups.get(group).end;
    }

    @Override
    public String group() {
        return group(0);
    }

    @Override
    public String group(int group) {
        checkGroup(group);
        if (groups.get(group).start < 0)
            return null;
        else
            return utf8input.subSequence(groups.get(group).start, groups.get(group).end).toString();
    }

    @Override
    public int groupCount() {
        checkGroup(0);
        return groups.size();
    }

    @Override
    public Iterator<MatchResult> iterator() {

        return new Iterator<MatchResult>() {
            boolean moved = false;
            boolean hasnext = false;
            @Override
            public boolean hasNext() {
                if (!moved) {
                    hasnext = findNext();
                    moved = true;
                }
                return hasnext;
            }
            @Override
            public MatchResult next() {
                if (hasNext()) {
                    moved = false;
                    return RE2Matcher.this;
                } else
                    throw new NoSuchElementException();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(matched);
        for (int i=0; i<groups.size(); i++) {
            buffer.append(String.format("\n%3d [%4d,%4d] %s", i, start(i), end(i), group(i)));
        }
        return buffer.toString();
    }
}
