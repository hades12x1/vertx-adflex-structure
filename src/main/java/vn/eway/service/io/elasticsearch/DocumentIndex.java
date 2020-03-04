package vn.eway.service.io.elasticsearch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.plugins.Plugin;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@DataObject(generateConverter = true)
public class DocumentIndex implements Serializable {

    private TransportClient delegate;
    private String index;
    private String type;

    private ConnectionString connectionString;
    private Settings settings;
    final List<Class<? extends Plugin>> plugins = new ArrayList<>();

    protected Map<String, TransportAddress> transportAddressMap = new LinkedHashMap<>();
    protected ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    static {
        //=> Disable DNS Cache
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
    }

    public DocumentIndex(){ }

    /**
     * URI Example: elasticsearch://host:port/index.type
     *
     * @param uri
     */
    public void initDocumentIndex(String uri){
        this.connectionString = new ConnectionString(uri);
        this.index = this.connectionString.getIndex();
        this.type = this.connectionString.getType();

        this.connectionString.getOptions().put("client.transport.ignore_cluster_name", "true");
        settings = Settings.builder().put(this.connectionString.getOptions()).build();

        TransportClient.Builder builder = TransportClient.builder().settings(settings);

        for (Class<? extends Plugin> plugin : plugins) {
            builder.addPlugin(plugin);
        }

        delegate = builder.build();
        this.updateTransportAddresses();
        scheduler.scheduleAtFixedRate(this::updateTransportAddresses, 60, 60, TimeUnit.SECONDS);
    }


    public DocumentIndex(String json) {
        this(new JsonObject(json));
    }

    public DocumentIndex(JsonObject jsonObject) {
        vn.eway.service.io.elasticsearch.DocumentIndexConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        vn.eway.service.io.elasticsearch.DocumentIndexConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    private final void updateTransportAddresses() {
        int count = 0;
        try{
            for (String it : this.connectionString.getHosts()) {
                URI uri = new URI("elasticsearch://" + it);
                InetSocketTransportAddress newTransportAddress = new InetSocketTransportAddress(InetAddress.getByName(uri.getHost()), uri.getPort());
                TransportAddress oldTransportAddress = transportAddressMap.get(uri.getHost());

                if (oldTransportAddress != null && oldTransportAddress.sameHost(newTransportAddress)) {
                    //=> Ignore when TransportAddress IP does not change
                    continue;
                }

                delegate.addTransportAddress(newTransportAddress);
                transportAddressMap.put(uri.getHost(), newTransportAddress);

                if (oldTransportAddress != null) {
                    delegate.removeTransportAddress(oldTransportAddress);
                }
                count++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public GetRequestBuilder prepareGet(String id) {
        GetRequestBuilder getRequestBuilder = delegate.prepareGet(index, type, id);
        return getRequestBuilder;
    }

    public MultiGetRequestBuilder prepareMultiGet(List<String> ids) {
        MultiGetRequestBuilder multiGetRequestBuilder = delegate.prepareMultiGet();
        multiGetRequestBuilder.add(index, type, ids);
        return multiGetRequestBuilder;
    }

    public IndexRequestBuilder prepareIndex(Document document) {
        IndexRequestBuilder indexRequestBuilder = delegate.prepareIndex(index, type);
        Document cloneDocument = new Document(document);
        if (StringUtils.isNotBlank(cloneDocument.getString("_id"))) {
            indexRequestBuilder.setId(document.getString("_id"));
            cloneDocument.remove("_id");
        }
        indexRequestBuilder.setSource(cloneDocument);
        return indexRequestBuilder;
    }

    public BulkRequestBuilder prepareBulk(List<Document> documents) {
        BulkRequestBuilder bulkRequestBuilder = delegate.prepareBulk();
        for (Document document : documents) {
            IndexRequest indexRequest = Requests.indexRequest(index).type(type);

            Document cloneDocument = new Document(document);
            if (StringUtils.isNotBlank(cloneDocument.getString("_id"))) {
                indexRequest.id(cloneDocument.getString("_id"));
                cloneDocument.remove("_id");
            }
            indexRequest.source(cloneDocument);

            bulkRequestBuilder.add(indexRequest);
        }
        return bulkRequestBuilder;
    }

    public UpdateRequestBuilder prepareUpdate(String id, Document updateDocument) {
        Validate.notBlank(id, "id must be not empty");

        UpdateRequestBuilder updateRequestBuilder = delegate.prepareUpdate(index, type, id);
        updateRequestBuilder.setDoc(updateDocument);
        return updateRequestBuilder;
    }

    public UpdateRequestBuilder prepareUpsert(Document upsertDocument) {
        Validate.notBlank(upsertDocument.getString("_id"), "document._id must be not empty");

        Document cloneDocument = new Document(upsertDocument);
        String id = cloneDocument.getString("_id");
        cloneDocument.remove("_id");

        UpdateRequestBuilder updateRequestBuilder = delegate.prepareUpdate(index, type, id);
        updateRequestBuilder.setDoc(cloneDocument);
        updateRequestBuilder.setUpsert(cloneDocument);

        return updateRequestBuilder;
    }

    public DeleteRequestBuilder prepareDelete(String id) {
        DeleteRequestBuilder deleteRequestBuilder = delegate.prepareDelete(index, type, id);
        return deleteRequestBuilder;
    }

    public QueryRequestBuilder prepareQueryDocument(String query) {
        query = StringUtils.replaceAll(query, "\\{index/type\\}", "$index/$type");
        query = query.replaceAll("\\{index\\}", "$index");
        return new QueryRequestBuilder(delegate, query);
    }

    @Deprecated
    public CreateIndexRequestBuilder prepareCreate() {
        CreateIndexRequestBuilder createIndexRequestBuilder = delegate.admin().indices().prepareCreate(index);
        createIndexRequestBuilder.addMapping("_default_", "{\"dynamic_templates\":[{\"string_field\":{\"mapping\":{\"index\":\"not_analyzed\",\"type\":\"string\"},\"match_mapping_type\":\"string\",\"match\":\"*\"}}]}");
        return createIndexRequestBuilder;
    }

}
