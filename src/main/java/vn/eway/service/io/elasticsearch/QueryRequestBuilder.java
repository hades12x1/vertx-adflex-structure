package vn.eway.service.io.elasticsearch;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.support.PlainListenableActionFuture;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.plugin.nlpcn.ElasticJoinExecutor;
import org.elasticsearch.search.SearchHits;
import org.nlpcn.es4sql.domain.JoinSelect;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.query.*;
import org.nlpcn.es4sql.query.join.ESJoinQueryAction;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;

/**
 * Created by chi on 2/3/17.
 */
public class QueryRequestBuilder {

    protected Client client;
    protected String query;

    protected TimeValue keepAlive = new TimeValue(1, TimeUnit.MINUTES);
    protected boolean requestCache;
    protected boolean requestProfile;
    protected boolean requestScroll;
    protected boolean isAgg = false;
    protected QueryAction queryAction;
    protected Long offset;
    protected Long limit;
    protected SqlElasticRequestBuilder sqlElasticRequestBuilder;
    protected SearchRequestBuilder searchRequestBuilder;
    protected ElasticJoinExecutor elasticJoinExecutor;
    protected SearchScrollRequestBuilder searchScrollRequestBuilder;

    public QueryRequestBuilder(Client client, String query) {
        query = StringUtils.trimToEmpty(query);
        query = StringUtils.replaceAll(query, "[\t\n\r]", " ");

        this.query = query;
        this.client = client;

        requestScroll = !this.query.contains(" ");
        if (requestScroll) {
            searchScrollRequestBuilder = client.prepareSearchScroll(query);
            return;
        }

        try {
            queryAction = ESActionFactory.create(client, query);

            //=> Parse Query Action, Offset, Limit
            if (queryAction instanceof DefaultQueryAction || queryAction instanceof AggregationQueryAction) {
                Field selectField = queryAction.getClass().getDeclaredField("select");
                selectField.setAccessible(true);
                Select select = (Select) selectField.get(queryAction);

                this.offset = (long) select.getOffset();
                this.limit = (long) select.getRowCount();

                /**
                 * @author chipn@eway.vn
                 * Fix incorrect paging with Agg
                 */
                if (select.isAgg) {
                    isAgg = true;
                    select.setOffset(0);
                    select.setRowCount(0);
                }

                sqlElasticRequestBuilder = queryAction.explain();
                searchRequestBuilder = (SearchRequestBuilder) sqlElasticRequestBuilder.getBuilder();
            }

            if (queryAction instanceof ESJoinQueryAction) {
                //=> Parse Meta Data
                Field joinSelectField = queryAction.getClass().getDeclaredField("joinSelect");
                joinSelectField.setAccessible(true);
                JoinSelect joinSelect = (JoinSelect) joinSelectField.get(queryAction);

                this.offset = 0L;
                this.limit = (long) joinSelect.getTotalLimit();

                sqlElasticRequestBuilder = queryAction.explain();
                elasticJoinExecutor = ElasticJoinExecutor.createJoinExecutor(client, sqlElasticRequestBuilder);
            }
        } catch (Exception ex) {
            throw new RuntimeException("@Query Error: " + query, ex);
        }
    }

    public Long getLimit() {
        return limit;
    }

    public Long getOffset() {
        return offset;
    }

    public QueryRequestBuilder setRequestCache(boolean requestCache) {
        this.requestCache = requestCache;
        return this;
    }

    public QueryRequestBuilder setRequestProfile(boolean requestProfile) {
        this.requestProfile = requestProfile;
        return this;
    }

    public QueryRequestBuilder setKeepAlive(TimeValue keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public final ListenableActionFuture<QueryResponse<Document>> execute() {
        PlainListenableActionFuture<QueryResponse<Document>> future = new PlainListenableActionFuture<>(client.threadPool());
        execute(future);
        return future;
    }

    public final QueryResponse<Document> get() {
        return execute().actionGet();
    }

    public final QueryResponse<Document> get(TimeValue timeout) {
        return execute().actionGet(timeout);
    }

    public final QueryResponse<Document> get(String timeout) {
        return execute().actionGet(timeout);
    }

    public void execute(final ActionListener<QueryResponse<Document>> listener) {
        if (searchScrollRequestBuilder != null) {
            if (keepAlive != null) {
                searchScrollRequestBuilder.setScroll(keepAlive);
            }
            searchScrollRequestBuilder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    try {
                        QueryResponse<Document> queryResponse = parseHitsResponse(searchResponse);
                        listener.onResponse(queryResponse);
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    listener.onFailure(e);
                }
            });
            return;
        }

        if (searchRequestBuilder != null) {
            searchRequestBuilder.setPreference("_primary");
            searchRequestBuilder.setRequestCache(requestCache);
            searchRequestBuilder.setProfile(requestProfile);
            searchRequestBuilder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    try {
                        QueryResponse<Document> queryResponse = isAgg ? parseAggregationsResponse(searchResponse) : parseHitsResponse(searchResponse);
                        listener.onResponse(queryResponse);
                    } catch (Exception ex) {
                        onFailure(ex);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    listener.onFailure(e);
                }
            });
            return;
        }

        if (elasticJoinExecutor != null) {
            try {
                elasticJoinExecutor.run();
                SearchHits searchHits = elasticJoinExecutor.getHits();

                QueryResponse<Document> queryResponse = parseSearchHits(searchHits);
                listener.onResponse(queryResponse);
            } catch (Exception ex) {
                listener.onFailure(ex);
            }
            return;
        }
    }

    public void getAllWithScroll(final ActionListener<QueryResponse<Document>> listener) {
        TimeValue timeValue = new TimeValue(60000);
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setPreference("_primary");
            searchRequestBuilder.setRequestCache(requestCache);
            searchRequestBuilder.setProfile(requestProfile);
            searchRequestBuilder.setScroll(timeValue);
            searchRequestBuilder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    try {
                        QueryResponse<Document> queryResponse = null;
                        QueryResponse<Document> response = new QueryResponse<>();
                        List<Document> dataResponse = new LinkedList<>();
                        do {
                            queryResponse = isAgg ? parseAggregationsResponse(searchResponse) : parseHitsResponse(searchResponse);
                            int totalRecord = searchResponse.getHits().getHits().length;

                            dataResponse.addAll(queryResponse.getRows());
                            response.setTotal((response.getTotal() != null) ? (response.getTotal() + totalRecord) : totalRecord);
                            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(timeValue).execute().actionGet();
                        } while (searchResponse.getHits().getHits().length != 0);
                        response.setRows(dataResponse);

                        listener.onResponse(response);
                    } catch (Exception ex) {
                        onFailure(ex);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    listener.onFailure(e);
                }
            });
            return;
        }
    }

    protected QueryResponse<Document> parseHitsResponse(SearchResponse searchResponse) {
        QueryResponse<Document> queryResponse = new QueryResponse<>();
        queryResponse.setLimit(limit);
        queryResponse.setOffset(offset);
        queryResponse.setTook(searchResponse.getTookInMillis());

        queryResponse.setTotal(searchResponse.getHits().getTotalHits());
        queryResponse.setScrollId(searchResponse.getScrollId());

        List<Document> documents = new ObjectResultsExtractor(false, false, true).extractResults(searchResponse.getHits(), false).toDocuments();
        queryResponse.setRows(documents);
        return queryResponse;
    }

    protected QueryResponse<Document> parseAggregationsResponse(SearchResponse searchResponse) {
        QueryResponse<Document> queryResponse = new QueryResponse<>();
        queryResponse.setLimit(limit);
        queryResponse.setOffset(offset);
        queryResponse.setTook(searchResponse.getTookInMillis());

        ObjectResult objectResult = new ObjectResultsExtractor(false, false, true).extractResults(searchResponse.getAggregations(), false);
        queryResponse.setTotal((long) objectResult.getLines().size());

        //=> Fake Paging in Memory
        List<Document> documents = objectResult.toDocuments(offset, limit);
        queryResponse.setRows(documents);
        return queryResponse;
    }

    protected QueryResponse<Document> parseSearchHits(SearchHits searchHits) {
        QueryResponse<Document> queryResponse = new QueryResponse<>();
        queryResponse.setLimit(limit);
        queryResponse.setOffset(offset);
        queryResponse.setTotal(searchHits.getTotalHits());

        List<Document> documents = new ObjectResultsExtractor(false, false, true).extractResults(searchHits, false).toDocuments();
        queryResponse.setRows(documents);
        return queryResponse;
    }


    public Document explainQuery() {
        if (sqlElasticRequestBuilder != null) {
            return Document.parse(sqlElasticRequestBuilder.explain());
        } else {
            return new Document();
        }
    }

    public ListenableActionFuture executeDebug() {
        PlainListenableActionFuture future = new PlainListenableActionFuture<>(client.threadPool());
        executeDebug(future);
        return future;
    }

    public void executeDebug(final ActionListener<Document> listener) {
        if (searchScrollRequestBuilder != null) {
            if (keepAlive != null) {
                searchScrollRequestBuilder.setScroll(keepAlive);
            }
            searchScrollRequestBuilder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    try {
                        listener.onResponse(Document.parse(searchResponse.toString()));
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    listener.onFailure(e);
                }
            });
            return;
        }

        if (searchRequestBuilder != null) {
            searchRequestBuilder.setRequestCache(requestCache);
            searchRequestBuilder.setProfile(requestProfile);
            searchRequestBuilder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    try {
                        listener.onResponse(Document.parse(searchResponse.toString()));
                    } catch (Exception ex) {
                        onFailure(ex);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    listener.onFailure(e);
                }
            });
            return;
        }

        if (elasticJoinExecutor != null) {
            try {
                elasticJoinExecutor.run();
                SearchHits searchHits = elasticJoinExecutor.getHits();

                XContentBuilder contentBuilder = XContentFactory.jsonBuilder().prettyPrint();
                contentBuilder.startObject();
                searchHits.toXContent(contentBuilder, EMPTY_PARAMS);
                contentBuilder.endObject();

                listener.onResponse(Document.parse(contentBuilder.string()));
            } catch (Exception ex) {
                listener.onFailure(ex);
            }
            return;
        }
    }


}
