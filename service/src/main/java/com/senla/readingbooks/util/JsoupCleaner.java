package com.senla.readingbooks.util;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@UtilityClass
public class JsoupCleaner {
    private final Safelist SAFE_LIST = Safelist.relaxed()
            .addTags("img", "video", "source")
            .addAttributes("img", "align", "alt", "height", "src", "title", "width")
            .addAttributes("video", "src", "controls", "autoplay", "loop", "muted")
            .addAttributes("source", "src", "type");

    public String cleanHtmlContent(String htmlContent) {
        return Jsoup.clean(htmlContent, SAFE_LIST);
    }
}
