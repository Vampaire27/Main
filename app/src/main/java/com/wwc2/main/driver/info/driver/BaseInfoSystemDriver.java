package com.wwc2.main.driver.info.driver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * the base info system driver.
 *
 * @author wwc2
 * @date 2017/1/26
 */
public class BaseInfoSystemDriver extends BaseInfoDriver {

    /**TAG*/
    private static final String TAG = "BaseInfoSystemDriver";

    @Override
    public boolean isNetworkRoaming(Context context) {
        boolean ret = false;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.isNetworkRoaming();
            }
        }
        return ret;
    }

    @Override
    public boolean hasIccCard(Context context) {
        boolean ret = false;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.hasIccCard();
            }
        }
        return ret;
    }

    @Override
    public String getVoiceMailNumber(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getVoiceMailNumber();
            }
        }
        return ret;
    }

    @Override
    public String getVoiceMailAlphaTag(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getVoiceMailAlphaTag();
            }
        }
        return ret;
    }

    @Override
    public String getSubscriberId(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSubscriberId();
            }
        }
        return ret;
    }

    @Override
    public int getSimState(Context context) {
        int ret = TelephonyManager.SIM_STATE_UNKNOWN;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSimState();
            }
        }
        return ret;
    }

    @Override
    public String getSimSerialNumber(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSimSerialNumber();
            }
        }
        return ret;
    }

    @Override
    public String getSimOperatorName(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSimOperatorName();
            }
        }
        return ret;
    }

    @Override
    public String getSimOperator(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSimOperator();
            }
        }
        return ret;
    }

    @Override
    public String getSimCountryIso(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getSimCountryIso();
            }
        }
        return ret;
    }

    @Override
    public int getPhoneType(Context context) {
        int ret = TelephonyManager.PHONE_TYPE_NONE;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getPhoneType();
            }
        }
        return ret;
    }

    @Override
    public int getNetworkType(Context context) {
        int ret = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getNetworkType();
            }
        }
        return ret;
    }

    @Override
    public String getLine1Number(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getLine1Number();
            }
        }
        return ret;
    }

    @Override
    public String getIMEI(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getDeviceId();
            }
        }
        return ret;
    }

    @Override
    public String getIMEISV(Context context) {
        String ret = null;
        if (null != context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                ret = tm.getDeviceSoftwareVersion();
            }
        }
        return ret;
    }

    @Override
    public String getIpAddress(Context context) {
        ///M: DHCPV6 change feature
        final String INTERFACE_NAME = "wlan0";
        final int BEGIN_INDEX = 0;
        final int SEPARATOR_LENGTH = 2;

        NetworkInterface wifiNetwork = null;
        String addresses = "";
        try {
            wifiNetwork = NetworkInterface.getByName(INTERFACE_NAME);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        if (wifiNetwork == null) {
            Log.d(TAG, "wifiNetwork is null");
            return null;
        }
        Enumeration<InetAddress> enumeration = wifiNetwork.getInetAddresses();
        if (enumeration == null) {
            Log.d(TAG, "enumeration is null" );
            return null;
        }
        while (enumeration.hasMoreElements()) {
            InetAddress inet = enumeration.nextElement();
            String hostAddress = inet.getHostAddress();
            if (hostAddress.contains("%")) {
                hostAddress = hostAddress.substring(BEGIN_INDEX, hostAddress.indexOf("%"));// remove %10, %wlan0
            }
            Log.d(TAG, "InetAddress = " + inet.toString());
            Log.d(TAG, "hostAddress = " + hostAddress);
            if (inet instanceof Inet6Address) {
                Log.d(TAG, "IPV6 address = " + hostAddress);
                addresses += hostAddress + "; ";
            } else if (inet instanceof Inet4Address){
                Log.d(TAG, "IPV4 address = " + hostAddress);
                addresses = hostAddress + " " + addresses;
            }
        }
        Log.d(TAG, "IP addresses = " + addresses );
        if (addresses != "" && (addresses.endsWith(", ") || addresses.endsWith("; "))) {
            addresses = addresses.substring(BEGIN_INDEX, addresses.length() - SEPARATOR_LENGTH);
        } else if (addresses == "") {
            addresses = null;
        }
        Log.d(TAG, "The result of IP addresses = " + addresses );
        return addresses;
    }

    @Override
    public String getWifiIpAddresses(Context context) {
        String ret = null;
        if (null != context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (null != wifiManager) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (null != wifiInfo) {
                    ret = wifiInfo.getMacAddress();
                }
            }
        }
        return ret;
    }

    @Override
    public String getSerialNumber(Context context) {
        return Build.SERIAL;
    }
}
