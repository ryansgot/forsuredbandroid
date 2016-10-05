package com.forsuredb.testapp.model;

import android.support.annotation.CallSuper;

import com.forsuredb.testapp.BuildConfig;
import com.fsryan.forsuredb.api.adapter.FSGsonSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializableSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

public class TestJsonAdapterFactory implements FSSerializerFactory {

    public static final DocStoreTestBase.Adapter<DocStoreTestBase> dstBAdapter = new DocStoreTestBase.Adapter<DocStoreTestBase>(DocStoreTestBase.class) {};
    public static final DocStoreTestBase.Adapter<DocStoreIntPropertyExtension> dstIAdapter = new DocStoreTestBase.Adapter<DocStoreIntPropertyExtension>(DocStoreIntPropertyExtension.class) {

        @CallSuper
        protected DocStoreIntPropertyExtension updateInstanceFromJson(JsonObject jsonObject, DocStoreIntPropertyExtension obj, Type typeOfT, JsonDeserializationContext context) {
            JsonElement jsonElement = jsonObject.get("value");
            if (jsonElement != null && !jsonElement.isJsonNull()) {
                obj.setValue(jsonElement.getAsInt());
            }
            return super.updateInstanceFromJson(jsonObject, obj, typeOfT, context);
        }

        @CallSuper
        protected JsonElement updateJsonFromInstance(JsonObject jsonObject, DocStoreIntPropertyExtension src, Type typeOfSrc, JsonSerializationContext context) {
            jsonObject.addProperty("value", src.getValue());
            return super.updateJsonFromInstance(jsonObject, src, typeOfSrc, context);
        }
    };
    public static final DocStoreTestBase.Adapter<DocStoreDoublePropertyExtension> dstDAdapter = new DocStoreTestBase.Adapter<DocStoreDoublePropertyExtension>(DocStoreDoublePropertyExtension.class) {

        @CallSuper
        protected DocStoreDoublePropertyExtension updateInstanceFromJson(JsonObject jsonObject, DocStoreDoublePropertyExtension obj, Type typeOfT, JsonDeserializationContext context) {
            JsonElement jsonElement = jsonObject.get("value");
            if (jsonElement != null && !jsonElement.isJsonNull()) {
                obj.setValue(jsonElement.getAsDouble());
            }
            return super.updateInstanceFromJson(jsonObject, obj, typeOfT, context);
        }

        @CallSuper
        protected JsonElement updateJsonFromInstance(JsonObject jsonObject, DocStoreDoublePropertyExtension src, Type typeOfSrc, JsonSerializationContext context) {
            jsonObject.addProperty("value", src.getValue());
            return super.updateJsonFromInstance(jsonObject, src, typeOfSrc, context);
        }
    };

    @Override
    public FSSerializer create() {
        switch (BuildConfig.SERIALIZER) {
            case "gson":
                return new FSGsonSerializer(new GsonBuilder()
                        .registerTypeAdapter(DocStoreTestBase.class, dstBAdapter)
                        .registerTypeAdapter(DocStoreIntPropertyExtension.class, dstIAdapter)
                        .registerTypeAdapter(DocStoreDoublePropertyExtension.class, dstDAdapter)
                        .create());
            case "java_serializable":
                return new FSSerializableSerializer();
        }

        return null;
    }
}
