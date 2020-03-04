package vn.eway.service.io.elasticsearch;

import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chipn@eway.vn on 9/21/2016.
 */
public class ObjectResult {

    private final List<String> headers;
    private final List<List<Object>> lines;

    public ObjectResult(List<String> headers, List<List<Object>> lines) {
        this.headers = headers;
        this.lines = lines;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<Object>> getLines() {
        return lines;
    }

    public List<Map<String, Object>> toMaps() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (List<Object> line : lines) {
            Map<String, Object> row = new LinkedHashMap<>();
            int columnIndex = 0;
            for (Object element : line) {
                columnIndex++;
                if (element == null) {
                    continue;
                }
                row.put(headers.get(columnIndex), element);
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> toMaps(long offset, long limit) {
        if (offset <= 0 && limit <= 0) {
            return this.toMaps();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (offset > lines.size()) {
            return rows;
        }

        long currentRowIndex = -1;
        long maxRowIndex = offset + limit - 1;
        int columnIndex;

        for (List<Object> line : lines) {
            currentRowIndex++;
            if (currentRowIndex < offset) {
                continue;
            }
            if (currentRowIndex > maxRowIndex) {
                break;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            columnIndex = -1;
            for (Object element : line) {
                columnIndex++;
                if (element == null) {
                    continue;
                }
                map.put(headers.get(columnIndex), element);
            }
            rows.add(map);
        }
        return rows;
    }

    public List<Document> toDocuments() {
        List<Document> rows = new ArrayList<>();
        for (List<Object> line : lines) {
            Document document = new Document();
            int columnIndex = -1;
            for (Object element : line) {
                columnIndex++;
                if (element == null) {
                    continue;
                }
                document.put(headers.get(columnIndex), element);
            }
            rows.add(document);
        }
        return rows;
    }

    public List<Document> toDocuments(long offset, long limit) {
        if (offset <= 0 && limit <= 0) {
            return this.toDocuments();
        }

        List<Document> rows = new ArrayList<>();
        if (offset > lines.size()) {
            return rows;
        }

        long currentRowIndex = -1;
        long maxRowIndex = offset + limit - 1;
        int columnIndex;

        for (List<Object> line : lines) {
            currentRowIndex++;
            if (currentRowIndex < offset) {
                continue;
            }
            if (currentRowIndex > maxRowIndex) {
                break;
            }

            Document document = new Document();
            columnIndex = -1;
            for (Object element : line) {
                columnIndex++;
                if (element == null) {
                    continue;
                }
                document.put(headers.get(columnIndex), element);
            }
            rows.add(document);
        }
        return rows;
    }
}
