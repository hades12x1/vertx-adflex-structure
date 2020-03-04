package vn.eway.service.io.elasticsearch;

import java.util.ArrayList;
import java.util.List;

public class QueryResponse<M> {

    private String scrollId;
    private List<M> rows;
    private Long total;
    private Long took;
    private Long offset;
    private Long limit;

    public static <T> QueryResponse<T> create() {
        return new QueryResponse<T>();
    }

    public List<M> getRows() {
        if (rows == null) {
            rows = new ArrayList<>(0);
        }
        return rows;
    }

    public QueryResponse<M> setRows(List<M> rows) {
        this.rows = rows;
        return this;
    }

    public M getFirstRow() {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public Long getTotal() {
        return total;
    }

    public QueryResponse<M> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Long getTook() {
        return took;
    }

    public QueryResponse<M> setTook(Long took) {
        this.took = took;
        return this;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public Long getOffset() {
        return offset;
    }

    public QueryResponse<M> setOffset(Long offset) {
        this.offset = offset;
        return this;
    }

    public Long getLimit() {
        return limit;
    }

    public QueryResponse<M> setLimit(Long limit) {
        this.limit = limit;
        return this;
    }
}
