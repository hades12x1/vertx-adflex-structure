package vn.eway.service.io.elasticsearch;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link vn.eway.service.io.elasticsearch.DocumentIndex}.
 * NOTE: This class has been automatically generated from the {@link vn.eway.service.io.elasticsearch.DocumentIndex} original class using Vert.x codegen.
 */
public class DocumentIndexConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, DocumentIndex obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
      }
    }
  }

  public static void toJson(DocumentIndex obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(DocumentIndex obj, java.util.Map<String, Object> json) {
  }
}
