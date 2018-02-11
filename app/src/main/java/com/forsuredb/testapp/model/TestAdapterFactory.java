package com.forsuredb.testapp.model;

import android.support.annotation.CallSuper;

import com.forsuredb.testapp.BuildConfig;
import com.fsryan.forsuredb.api.adapter.FSDefaultSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class TestAdapterFactory implements FSSerializerFactory {

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
                return new GsonSerializer();
            case "java_serializable":
                return new FSDefaultSerializer();
        }

        return null;
    }

    private static class GsonSerializer implements FSSerializer {

        private static final Gson gson = new GsonBuilder()
                .registerTypeAdapter(DocStoreTestBase.class, dstBAdapter)
                .registerTypeAdapter(DocStoreIntPropertyExtension.class, dstIAdapter)
                .registerTypeAdapter(DocStoreDoublePropertyExtension.class, dstDAdapter)
                .create();

        @Override
        public boolean storeAsBlob() {
            return false;
        }

        @Override
        public String createStringDoc(Type type, Object o) {
            return gson.toJson(o, type);
        }

        @Override
        public byte[] createBlobDoc(Type type, Object o) {
            return createStringDoc(type, o).getBytes();
        }

        @Override
        public <T> T fromStorage(Type type, byte[] bytes) {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            InputStreamReader isReader = new InputStreamReader(is);
            JsonReader jsonReader = new JsonReader(isReader);
            try {
                return gson.fromJson(jsonReader, type);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    is.close();
                    isReader.close();
                    jsonReader.close();
                } catch (IOException ioe) {}
            }
        }

        @Override
        public <T> T fromStorage(Type type, String s) {
            return gson.fromJson(s, type);
        }
    }
}
