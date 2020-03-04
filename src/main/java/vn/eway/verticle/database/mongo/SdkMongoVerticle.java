package vn.eway.verticle.database.mongo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static vn.eway.constant.Constant.*;
import vn.eway.service.MongoRepository;

public class SdkMongoVerticle extends AbstractVerticle {
    private static Logger LOGGER = LogManager.getLogger(SdkMongoVerticle.class);
    private MongoClient sdkMongo;

    @Override
    public void start(Promise<Void> promise) {
        try {
            String uri = config().getString("mongo.sdk.uri");
            String dataSourceName = this.getClass().getName();
            Validate.notEmpty(uri, "Uri mongo.sdk.uri must be not empty.");
            JsonObject configMongoSdk = new JsonObject().put("connection_string", uri);

            this.sdkMongo = MongoClient.createShared(vertx, configMongoSdk, dataSourceName);

            MongoRepository mongoRepository = MongoRepository.create(this.sdkMongo);
            new ServiceBinder(vertx)
                    .setAddress(ChannelAddress.BUS_MONGO_SDK)
                    .register(MongoRepository.class, mongoRepository);

            promise.complete();
        } catch (Exception ex) {
            LOGGER.error("@SdkMongoVerticle error: " + ex.getCause());
            promise.fail(ex);
        }
    }

    @Override
    public void stop() {
        if (sdkMongo != null) {
            sdkMongo.close();
        }
    }
}
