package com.example.memo_demo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StringProcessorUnitTest {
    @Test
    public void editor_isCorrect() {
        byte[] bytes = StringProcessor.htmlToByteArray("hello");
        String html = StringProcessor.decodeByteArray(bytes).data;
        int type = StringProcessor.decodeByteArray(bytes).type;
        assertEquals("hello", html);
        assertEquals(type, StringProcessor.editor);
    }

    @Test
    public void status_isCorrect() {
        byte[] bytes = StringProcessor.statusToByteArray("status");
        String html = StringProcessor.decodeByteArray(bytes).data;
        int type = StringProcessor.decodeByteArray(bytes).type;
        assertEquals("status", html);
        assertEquals(type, StringProcessor.status);
    }
}
