package vn.eway.service.io.elasticsearch.index;

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import vn.eway.service.io.elasticsearch.DocumentIndex;

import java.util.ArrayList;
import java.util.List;

public class ConversionIndex extends DocumentIndex {
    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyMMdd");
    protected EsConversionHolder holder;
    private Vertx vertx;
    private static final String DS_LOCAL_MAP_NAME = "__vertx.ElasticSearch.Conversion.datasources";

    public ConversionIndex createConversionIndex(Vertx vertx, String uri){
        this.vertx = vertx;
        this.holder = lookupHolder("ConversionIndexSource", uri);
        return holder.esConversion;
    }

    /**
     * URI Example: elasticsearch://host:port/index.type
     * @param uri
     */
    public ConversionIndex(String uri) {
        initDocumentIndex(uri);
    }

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
            indexNames.add("conversions_" + start.toString("yyMMdd"));
            start = start.plusDays(1);
        }
        return indexNames;
    }

    static List<String> extractHotIndexNameByDate(LocalDate start, LocalDate end) {
        List<String> indexNames = new ArrayList<>();

        LocalDate beginOfConversionDate = new LocalDate(2016, 12, 01);

        if (start.compareTo(beginOfConversionDate) < 0) {
            start = beginOfConversionDate;
        }

        if (end.compareTo(LocalDate.now()) > 0) {
            end = LocalDate.now();
        }

        while (start.compareTo(end) <= 0) {
            indexNames.add("hot_conversions_" + start.toString("yyMMdd"));
            start = start.plusDays(1);
        }
        return indexNames;
    }

    private EsConversionHolder lookupHolder(String datasourceName, String uri) {
        synchronized (vertx) {
            LocalMap<String, EsConversionHolder> map = vertx.sharedData().getLocalMap(DS_LOCAL_MAP_NAME);
            EsConversionHolder theHolder = map.get(datasourceName);
            if (theHolder == null) {
                theHolder = new EsConversionHolder(uri, () -> removeFromMap(map, datasourceName));
                map.put(datasourceName, theHolder);
            } else {
                theHolder.incRefCount();
            }
            return theHolder;
        }
    }

    private void removeFromMap(LocalMap<String, EsConversionHolder> map, String dataSourceName) {
        synchronized (vertx) {
            map.remove(dataSourceName);
            if (map.isEmpty()) {
                map.close();
            }
        }
    }

    private static class EsConversionHolder implements Shareable {
        ConversionIndex esConversion;
        String uri;
        Runnable closeRunner;
        int refCount = 1;

        public EsConversionHolder(String uri, Runnable closeRunner) {
            this.closeRunner = closeRunner;
            this.uri = uri;
        }

        synchronized DocumentIndex elasticSearch() {
            if (esConversion == null) {
                esConversion = new ConversionIndex(uri);
            }
            return esConversion;
        }

        synchronized void incRefCount() {
            refCount++;
        }

        synchronized void close() {
            if (--refCount == 0) {
                if (closeRunner != null) {
                    closeRunner.run();
                }
            }
        }

    }
}
