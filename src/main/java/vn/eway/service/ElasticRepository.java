package vn.eway.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceproxy.ProxyHelper;
import vn.eway.service.impl.ElasticRepositoryImpl;
import vn.eway.service.io.elasticsearch.DocumentIndex;

@ProxyGen
@VertxGen
public interface ElasticRepository {

    static ElasticRepository create(DocumentIndex documentIndex){
        return new ElasticRepositoryImpl(documentIndex);
    }

    static ElasticRepository createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(ElasticRepository.class, vertx, address);
    }

    void findDocumentByQuery(String query, Handler<AsyncResult<JsonArray>> resultHandler);
}
