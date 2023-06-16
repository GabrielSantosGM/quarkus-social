package io.github.akinicchi.quarkus_social.utils;

public class CompareUtils {

    private CompareUtils(){
    }

    public static boolean isEqualsId(Long userId, Long followerId) {
        return userId.equals(followerId);
    }
}