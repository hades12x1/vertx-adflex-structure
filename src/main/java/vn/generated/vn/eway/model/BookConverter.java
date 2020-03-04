package vn.eway.model;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link vn.eway.model.Book}.
 * NOTE: This class has been automatically generated from the {@link vn.eway.model.Book} original class using Vert.x codegen.
 */
public class BookConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Book obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "id":
          if (member.getValue() instanceof String) {
            obj.setId((String)member.getValue());
          }
          break;
        case "name":
          if (member.getValue() instanceof String) {
            obj.setName((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(Book obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Book obj, java.util.Map<String, Object> json) {
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
  }
}
