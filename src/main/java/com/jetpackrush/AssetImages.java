package com.jetpackrush;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

final class AssetImages {
    private static final Map<String, Image> CACHE = new HashMap<>();

    private AssetImages() {
    }

    static Image get(String path) {
        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }
        InputStream in = AssetImages.class.getResourceAsStream(path);
        Image image = in == null ? null : new Image(in);
        CACHE.put(path, image);
        return image;
    }
}
