package com.forsuredb.testapp.model;

import android.support.annotation.CallSuper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.Date;

public abstract class DocStoreTestBase {
    private String uuid;
    private String name;
    private Date date;

    public DocStoreTestBase() {}

    public DocStoreTestBase(String uuid, String name, Date date) {
        this.uuid = uuid;
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public static abstract class Adapter<T extends DocStoreTestBase> extends TypeAdapter<T> {

        public Adapter(Class<T> cls) {
            super(cls);
        }

        @CallSuper
        protected T updateInstanceFromJson(JsonObject jsonObject, T obj, Type typeOfT, JsonDeserializationContext context) {
            JsonElement jsonElement = jsonObject.get("uuid");
            obj.setUuid(jsonElement == null || jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            jsonElement = jsonObject.get("name");
            obj.setName(jsonElement == null || jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            obj.setDate(deserializeDate(jsonObject, "date"));
            return obj;
        }

        @CallSuper
        protected JsonElement updateJsonFromInstance(JsonObject jsonObject, T src, Type typeOfSrc, JsonSerializationContext context) {
            jsonObject.addProperty("uuid", src.getUuid());
            jsonObject.addProperty("name", src.getName());
            jsonObject.addProperty("date", serializeDate(src.getDate()));
            return jsonObject;
        }
    }
}
