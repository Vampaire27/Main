package com.wwc2.main.driver.network.wifi.ap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dongseok0 on 9/11/2015.
 */
public class SupplicantState {
    static private Method   isHandshakeState;
    static {
        try {
            isHandshakeState = android.net.wifi.SupplicantState.class.getDeclaredMethod("isHandshakeState", android.net.wifi.SupplicantState.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public static boolean isHandshake(android.net.wifi.SupplicantState state) {
        try {
            return (boolean)isHandshakeState.invoke(null, state);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
