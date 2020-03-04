package vn.eway.business.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vn.eway.business.BookBusiness;
import vn.eway.constant.Constant;
import vn.eway.service.MongoRepository;

public class BookBusinessImpl implements BookBusiness {

    private final MongoRepository mongoMeraRepository;
    private final Vertx vertx;

    public BookBusinessImpl(Vertx vertx){
        this.vertx = vertx;
        mongoMeraRepository = MongoRepository.createProxy(vertx, Constant.ChannelAddress.BUS_MONGO_META);
    }

    @Override
    public void inrichmentField(JsonObject jsonObject, Handler<AsyncResult<JsonObject>> resultHandler) {
        jsonObject.put("service", "inrichment field - chuyenns");
        mongoMeraRepository.insert("books", jsonObject, res -> {
            if(res.succeeded()){
                resultHandler.handle(Future.succeededFuture(res.result()));
            }else {
                resultHandler.handle(Future.failedFuture(new RuntimeException("Insert error...")));
            }
        });
    }

}
