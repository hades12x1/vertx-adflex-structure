package vn.eway.service.io.elasticsearch.index;

import org.joda.time.LocalDate;
import vn.eway.service.io.elasticsearch.DocumentIndex;

import java.util.ArrayList;
import java.util.List;

public class ConversionPaymentIndex extends DocumentIndex {

    /**
     * URI Example: elasticsearch://host:port/index.type
     * @param uri
     */
    public ConversionPaymentIndex(String uri) {
        initDocumentIndex(uri);
    }

    static List<String> extractIndexNameByDate(LocalDate start, LocalDate end) {
        List<String> indexNames = new ArrayList<>();

        LocalDate beginOfConversionDate = new LocalDate(2016, 12, 01);

        if (start.compareTo(end) < 0) {
            start = beginOfConversionDate;
        }

        if (end.compareTo(LocalDate.now()) > 0) {
            end = LocalDate.now();
        }

        while (start.compareTo(end) <= 0) {
            indexNames.add("hot_payment_conversion_" + start.toString("yyMMdd"));
            start = start.plusDays(1);
        }
        return indexNames;
    }

}
