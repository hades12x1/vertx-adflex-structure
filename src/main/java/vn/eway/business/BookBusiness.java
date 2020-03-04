package vn.eway.business;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import vn.eway.business.impl.BookBusinessImpl;

@ProxyGen
@VertxGen
public interface BookBusiness {

    static BookBusiness create(Vertx vertx){
        return new BookBusinessImpl(vertx);
    }

    static BookBusiness createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(BookBusiness.class, vertx, address);
    }

    void inrichmentField(JsonObject jsonObject, Handler<AsyncResult<JsonObject>> resultHandler);

}
