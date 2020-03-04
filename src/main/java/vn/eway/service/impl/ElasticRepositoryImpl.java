package vn.eway.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import org.bson.Document;
import vn.eway.service.ElasticRepository;
import vn.eway.service.io.elasticsearch.DocumentIndex;

import java.util.List;

public class ElasticRepositoryImpl implements ElasticRepository {
    private final DocumentIndex index;

    public ElasticRepositoryImpl(DocumentIndex documentIndex){
        this.index = documentIndex;
    }

    @Override
    public void findDocumentByQuery(String query, Handler<AsyncResult<JsonArray>> resultHandler) {
        try{
            List<Document> result = index.prepareQueryDocument(query).get().getRows();
            resultHandler.handle(Future.succeededFuture(new JsonArray(result)));
        }catch (Exception ex){
            Future.failedFuture(new RuntimeException("Error query with es: " + query));
        }
    }

}
