package vn.eway.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;
import vn.eway.service.impl.MongoRepositoryImpl;

import java.util.List;

@ProxyGen
@VertxGen
public interface MongoRepository {

    static MongoRepository create(MongoClient mongoClient){
        return new MongoRepositoryImpl(mongoClient);
    }

	static MongoRepository createProxy(Vertx vertx, String address) {
		return ProxyHelper.createProxy(MongoRepository.class, vertx, address);
	}

    void insert(String entityName, JsonObject entity, Handler<AsyncResult<JsonObject>> resultHandler);

    void update(String entityName, JsonObject query, JsonObject modelUpdate, Handler<AsyncResult<JsonObject>> resultHandler);

    void delete(String entityName, JsonObject query, Handler<AsyncResult<Boolean>> resultHandler);

    void findById(String entityName, String id, Handler<AsyncResult<JsonObject>> resultHandler);

    void findAll(String entityName, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    void close();
}
