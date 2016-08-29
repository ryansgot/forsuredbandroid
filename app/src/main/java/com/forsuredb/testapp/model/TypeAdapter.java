package com.forsuredb.testapp.model;


import android.support.annotation.CallSuper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

public abstract class TypeAdapter<T extends DocStoreTestBase> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final Class<T> cls;

    public TypeAdapter(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public final JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return updateJsonFromInstance(new JsonObject(), src, typeOfSrc, context);
    }

    @Override
    public final T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return updateInstanceFromJson(json.getAsJsonObject(), cls.newInstance(), typeOfT, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract JsonElement updateJsonFromInstance(JsonObject jsonObject, T src, Type typeOfSrc, JsonSerializationContext context);
    protected abstract T updateInstanceFromJson(JsonObject asJsonObject, T t, Type typeOfT, JsonDeserializationContext context);

    protected final long serializeDate(Date date) {
        return date == null ? -1L : date.getTime();
    }

    protected final Date deserializeDate(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null || jsonElement.isJsonNull() ? null : new Date(jsonElement.getAsLong());
    }
}
