package vn.eway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

@DataObject(generateConverter = true)
public class Book implements Serializable {

	@JsonProperty("name")
	private String name;

	@JsonProperty("_id")
	private String id;

	public Book() {
	}

	public Book(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Book(String json) {
		this(new JsonObject(json));
	}

	public Book(JsonObject jsonObject) {
		vn.eway.model.BookConverter.fromJson(jsonObject, this);
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		vn.eway.model.BookConverter.toJson(this, jsonObject);
		return jsonObject;
	}

	@Override
	public String toString() {
		return "{\n" +
			"    \"id\": " + this.id + ",\n" +
			"    \"name\": \"" + this.name + "\"\n" +
			"}";
	}
}
