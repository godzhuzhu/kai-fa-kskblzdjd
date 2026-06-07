package cn.edu.whut.sept.zuul.game.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StoreUtil {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static String toJson(Store store) {
        return GSON.toJson(store);
    }

    public static Store fromJson(String json) {
        return GSON.fromJson(json, Store.class);
    }
}
