package com.wwc2.main.driver.steer.driver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/1/21.
 */
public class SteerKeysDefine {
    public static final byte KEY_VALUE_MODE = (byte) 0x01;
    public static final byte KEY_VALUE_DVD = (byte) 0x02;
    public static final byte KEY_VALUE_GPS = (byte) 0x03;
    public static final byte KEY_VALUE_RADIO = (byte) 0x04;
    public static final byte KEY_VALUE_BLUETOOTH = (byte) 0x05;
    public static final byte KEY_VALUE_PLAY_PAUSE = (byte) 0x06;
    public static final byte KEY_VALUE_PREVIEW = (byte) 0x07;
    public static final byte KEY_VALUE_NEXT = (byte) 0x08;
    public static final byte KEY_VALUE_PHONE_ACCEPT = (byte) 0x09;
    public static final byte KEY_VALUE_PHONE_HANGUP = (byte) 0x0a;
    public static final byte KEY_VALUE_MUTE = (byte) 0x0b;
    public static final byte KEY_VALUE_VOLUME_DOWN = (byte) 0x0c;
    public static final byte KEY_VALUE_VOLUME_UP = (byte) 0x0d;
    public static final byte KEY_VALUE_FINE_TUNE_ADD = (byte) 0x0e;
    public static final byte KEY_VALUE_FINE_TUNE_DEC = (byte) 0x0f;
    public static final byte KEY_VALUE_HOME = (byte) 0x10;
    public static final byte KEY_VALUE_BACK = (byte) 0x11;
    public static final byte KEY_VALUE_POWER = (byte) 0x12;
    public static final byte KEY_VALUE_EQ = (byte) 0x13;
    public static final byte KEY_VALUE_BLUETOOTH_MUSIC = (byte) 0x14;

    public static final byte KEY_ID_MODE = (byte) 0x01;
    public static final byte KEY_ID_DVD = (byte) 0x02;
    public static final byte KEY_ID_GPS = (byte) 0x03;
    public static final byte KEY_ID_RADIO = (byte) 0x04;
    public static final byte KEY_ID_BLUETOOTH = (byte) 0x05;
    public static final byte KEY_ID_PLAY_PAUSE = (byte) 0x06;
    public static final byte KEY_ID_PREVIEW = (byte) 0x07;
    public static final byte KEY_ID_NEXT = (byte) 0x08;
    public static final byte KEY_ID_PHONE_ACCEPT = (byte) 0x09;
    public static final byte KEY_ID_PHONE_HANGUP = (byte) 0x0a;
    public static final byte KEY_ID_MUTE = (byte) 0x0b;
    public static final byte KEY_ID_VOLUME_DOWN = (byte) 0x0c;
    public static final byte KEY_ID_VOLUME_UP = (byte) 0x0d;
    public static final byte KEY_ID_FINE_TUNE_ADD = (byte) 0x0e;
    public static final byte KEY_ID_FINE_TUNE_DEC = (byte) 0x0f;
    public static final byte KEY_ID_HOME = (byte) 0x10;
    public static final byte KEY_ID_BACK = (byte) 0x11;
    public static final byte KEY_ID_POWER = (byte) 0x12;
    public static final byte KEY_ID_EQ = (byte) 0x13;
    public static final byte KEY_ID_BLUETOOTH_MUSIC = (byte) 0x14;

    public static SteerKeysMap<Byte, Byte> mKeys;

    static {
        mKeys = new SteerKeysMap<Byte, Byte>() {{
            put(KEY_ID_MODE, KEY_VALUE_MODE);
            put(KEY_ID_DVD, KEY_VALUE_DVD);
            put(KEY_ID_GPS, KEY_VALUE_GPS);
            put(KEY_ID_RADIO, KEY_VALUE_RADIO);
            put(KEY_ID_BLUETOOTH, KEY_VALUE_BLUETOOTH);
            put(KEY_ID_PLAY_PAUSE, KEY_VALUE_PLAY_PAUSE);
            put(KEY_ID_PREVIEW, KEY_VALUE_PREVIEW);
            put(KEY_ID_NEXT, KEY_VALUE_NEXT);
            put(KEY_ID_PHONE_ACCEPT, KEY_VALUE_PHONE_ACCEPT);
            put(KEY_ID_PHONE_HANGUP, KEY_VALUE_PHONE_HANGUP);
            put(KEY_ID_MUTE, KEY_VALUE_MUTE);
            put(KEY_ID_VOLUME_DOWN, KEY_VALUE_VOLUME_DOWN);
            put(KEY_ID_VOLUME_UP, KEY_VALUE_VOLUME_UP);
            put(KEY_ID_FINE_TUNE_ADD, KEY_VALUE_FINE_TUNE_ADD);
            put(KEY_ID_FINE_TUNE_DEC, KEY_VALUE_FINE_TUNE_DEC);
            put(KEY_ID_HOME, KEY_VALUE_HOME);
            put(KEY_ID_BACK, KEY_VALUE_BACK);
            put(KEY_ID_POWER, KEY_VALUE_POWER);
            put(KEY_ID_EQ, KEY_VALUE_EQ);
            put(KEY_ID_BLUETOOTH_MUSIC, KEY_VALUE_BLUETOOTH_MUSIC);
        }};
    }

    public static boolean containsValue(Byte v) {
        return mKeys.containsValue(v);
    }

    public static boolean containsID(Byte i) {
        return mKeys.containsID(i);
    }

    public static Byte getValue(Byte k) {
        return mKeys.getValue(k);
    }

    public static Byte getID(Byte v) {
        return mKeys.getKey(v);
    }

    static class SteerKeysMap<I, V> {
        Map<I, V> mIDsMap = new HashMap<>();
        Map<V, I> mValuesMap = new HashMap<>();

        public boolean containsValue(V v) {
            return mValuesMap.containsKey(v);
        }

        public boolean containsID(I i) {
            return mIDsMap.containsKey(i);
        }

        public V getValue(I i) {
            return mIDsMap.get(i);
        }

        public I getKey(V v) {
            return mValuesMap.get(v);
        }

        public void put(I i, V v) {
            mIDsMap.put(i, v);
            mValuesMap.put(v, i);
        }
    }
}
