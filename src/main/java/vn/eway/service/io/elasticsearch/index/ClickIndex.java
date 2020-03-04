package vn.eway.service.io.elasticsearch.index;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import vn.eway.service.io.elasticsearch.DocumentIndex;

import java.util.ArrayList;
import java.util.List;

public class ClickIndex extends DocumentIndex {
    /**
     * URI Example: elasticsearch://host:port/index.type
     * @param uri
     */
    public ClickIndex(String uri) {

        super.initDocumentIndex(uri);
    }

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyMMdd");

    static List<String> extractIndexNameByDate(LocalDate start, LocalDate end) {
        List<String> indexNames = new ArrayList<>();

        LocalDate beginOfConversionDate = new LocalDate(2016, 12, 01);

        if (start.compareTo(beginOfConversionDate) < 0) {
            start = beginOfConversionDate;
        }

        if (end.compareTo(LocalDate.now()) > 0) {
            end = LocalDate.now();
        }

        while (start.compareTo(end) <= 0) {
            indexNames.add("clicks_" + start.toString("yyMMdd"));
            start = start.plusDays(1);
        }
        return indexNames;
    }
}
