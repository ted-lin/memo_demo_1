package com.example.memo_demo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TextHelperUnitTest {

    @Test
    public void bold_isCorrect() {
        String text1 = TextHelper.toPlainTxt("<b>123</b>");
        String expect = "123";
        assertEquals(expect, text1);
    }

    @Test
    public void noContent_isCorrect() {
        String text1 = TextHelper.toPlainTxt("<h></h>");
        String expect = "";
        assertEquals(expect, text1);
    }

    @Test
    public void br_isCorrect() {
        String text1;
        text1 = TextHelper.toPlainTxt("<br>");
        String expect = "\n";
        assertEquals(expect, text1);
        text1 = TextHelper.toPlainTxt("<br>133<br>");
        expect = "\n133\n";
        assertEquals(expect, text1);
        text1 = TextHelper.toPlainTxt("< br>1233<br >");
        expect = "\n1233\n";
        assertEquals(expect, text1);
    }

    @Test
    public void noContentTag_isCorrect() {
        String text1 = TextHelper.toPlainTxt("<x/>");
        String expect = "";
        assertEquals(expect, text1);
    }

    @Test
    public void styles_isCorrect() {
        String text1 = TextHelper.toPlainTxt("<h id=\"1234\">hello<h>");
        String expect = "hello";
        assertEquals(expect, text1);
    }

    @Test
    public void space_isCorrect() {
        String text1 = TextHelper.toPlainTxt("&nbsp;");
        String expect = " ";
        assertEquals(expect, text1);
    }

    @Test
    public void space_to_html_isCorrect() {
        String text1 = TextHelper.toHtml(" ");
        String expect = "&nbsp;";
        assertEquals(expect, text1);
    }

    @Test
    public void less_than_to_html_isCorrect() {
        String text1 = TextHelper.toHtml("<");
        String expect = "&lt;";
        assertEquals(expect, text1);
    }

    @Test
    public void great_than_to_html_isCorrect() {
        String text1 = TextHelper.toHtml(">");
        String expect = "&gt;";
        assertEquals(expect, text1);
    }

    @Test
    public void ampe_to_html_isCorrect() {
        String text1 = TextHelper.toHtml("&");
        String expect = "&amp;";
        assertEquals(expect, text1);
    }

    @Test
    public void quot_to_html_isCorrect() {
        String text1 = TextHelper.toHtml("\"");
        String expect = "&quot;";
        assertEquals(expect, text1);
    }

    @Test
    public void apos_to_html_isCorrect() {
        String text1 = TextHelper.toHtml("\'");
        String expect = "&apos;";
        assertEquals(expect, text1);
    }

    @Test
    public void yen_to_html_isCorrect() {
        String text1 = TextHelper.toHtml("¥");
        String expect = "&yen;";
        assertEquals(expect, text1);
    }

}

