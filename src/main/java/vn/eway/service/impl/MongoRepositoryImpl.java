package vn.eway.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.apache.commons.lang3.Validate;
import vn.eway.service.MongoRepository;

import java.util.List;

public class MongoRepositoryImpl implements MongoRepository {
    private final MongoClient mongoClient;

    public MongoRepositoryImpl(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void insert(String entityName, JsonObject entity, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.mongoClient.insert(entityName, entity, res -> {
            if(res.succeeded()){
                resultHandler.handle(Future.succeededFuture(entity));
            }else{
                resultHandler.handle(Future.failedFuture("@Cannot insert model."));
            }
        });
    }

    @Override
    public void update(String entityName, JsonObject query, JsonObject modelUpdate, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.mongoClient.findOneAndUpdate(entityName, query, modelUpdate, res ->{
            if(res.succeeded()){
                resultHandler.handle(Future.succeededFuture(res.result()));
            }else{
                resultHandler.handle(Future.failedFuture("@Cannot update model."));
            }
        });
    }

    @Override
    public void delete(String entityName, JsonObject query, Handler<AsyncResult<Boolean>> resultHandler) {
        this.mongoClient.findOneAndDelete(entityName, query, res -> {
             if(res.succeeded()){
                 resultHandler.handle(Future.succeededFuture(Boolean.TRUE));
             }else{
                 resultHandler.handle(Future.succeededFuture(Boolean.FALSE));
             }
        });
    }

    @Override
    public void findById(String entityName, String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            Validate.notEmpty(id, "id must be not empty.");
            JsonObject query = new JsonObject().put("_id", id);
            this.mongoClient.find(entityName, query, res -> {
                if (res.succeeded() && res.result().size() > 0) {
                    resultHandler.handle(Future.succeededFuture(res.result().get(0)));
                } else {
                    resultHandler.handle(Future.succeededFuture(null));
                }
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void findAll(String entityName, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        this.mongoClient.find(entityName, new JsonObject(), res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(new Throwable("@Error query with entity.")));
            }
        });
    }

    public void findByEntity(String entityName, JsonObject query, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        this.mongoClient.find(entityName, query, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(new Throwable("@Error query with entity.")));
            }
        });
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }
}
