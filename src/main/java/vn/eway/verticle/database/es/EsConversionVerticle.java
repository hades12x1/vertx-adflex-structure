package vn.eway.verticle.database.es;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static vn.eway.constant.Constant.*;
import vn.eway.service.ElasticRepository;
import vn.eway.service.io.elasticsearch.index.ConversionIndex;

public class EsConversionVerticle extends AbstractVerticle {
    private static Logger LOGGER = LogManager.getLogger(EsConversionVerticle.class);
    private ConversionIndex esConversion;

    @Override
    public void start(Promise<Void> promise) {
        try {
            String uri = config().getString("es.conversion.uri");
            Validate.notEmpty(uri, "Uri es.conversion.uri must be not empty.");

            this.esConversion = new ConversionIndex(uri);

            ElasticRepository elasticSearch = ElasticRepository.create(this.esConversion);
            new ServiceBinder(vertx)
                    .setAddress(ChannelAddress.BUS_ES_CONVERSION)
                    .register(ElasticRepository.class, elasticSearch);

            promise.complete();
        } catch (Exception ex) {
            LOGGER.error("@EsConversionVerticle error: " + ex.getCause());
            promise.fail(ex);
        }
    }

}
