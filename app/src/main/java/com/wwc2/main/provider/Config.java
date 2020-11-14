package com.wwc2.main.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The configure of the ContentProvider.
 *
 * @author wwc2
 * @date 2017/1/24
 */
public class Config {

    /**
     * When change the database data structure, then increase the version.
     * reason to create the database newly.
     */
    public static final int DATABASE_VERSION = 2;

    /**
     * the database name.
     */
    public static final String DATABASE_NAME = "logic.db";

    /**
     * The map of the ContentProvider elements.
     */
    public static Map<String, String> mElementMap = new ConcurrentHashMap<String, String>();
}
