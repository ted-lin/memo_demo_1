package com.example.memo_demo;


public class TextHelper {

    public static String toPlainTxt(String html) {
        return html.replaceAll("<[ ]*br[ ]*/?[ ]*>", "\n")
                .replaceAll("<[^>]*[^\\\\s>][^>]*>", "")
                .replaceAll("<[^>]*[^\\\\s>][^>]*/>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">");

    }

    public static String toHtml(String plainText) {
        return plainText.replaceAll(" ", "&nbsp;")
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\n", "<br>");
    }
}
