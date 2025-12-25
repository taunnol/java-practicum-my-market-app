package ru.yandex.practicum.mymarket.common.util;

public final class ImgPathUtils {

    private ImgPathUtils() {
    }

    public static String normalize(String imgPath) {
        if (imgPath == null) {
            return "";
        }
        return imgPath.startsWith("/") ? imgPath.substring(1) : imgPath;
    }
}
