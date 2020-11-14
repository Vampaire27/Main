package com.wwc2.main.driver.info;

import android.content.Context;

/**
 * the device info driver interface.
 *
 * @author wwc2
 * @date 2017/1/26
 */
public interface InfoDriverable {

    /**
     * 是否漫游:
     * (在GSM用途下)
     */
    boolean isNetworkRoaming(Context context);

    /**
     * ICC卡是否存在
     */
    boolean hasIccCard(Context context);

    /**
     * 获取语音邮件号码：
     * 需要权限：READ_PHONE_STATE
     */
    String getVoiceMailNumber(Context context);

    /**
     * 取得和语音邮件相关的标签，即为识别符
     * 需要权限：READ_PHONE_STATE
     */
    String getVoiceMailAlphaTag(Context context);

    /**
     * 唯一的用户ID：
     * 例如：IMSI(国际移动用户识别码) for a GSM phone.
     * 需要权限：READ_PHONE_STATE
     */
    String getSubscriberId(Context context);

    /**
     * SIM的状态信息：
     * SIM_STATE_UNKNOWN          未知状态 0
     * SIM_STATE_ABSENT           没插卡 1
     * SIM_STATE_PIN_REQUIRED     锁定状态，需要用户的PIN码解锁 2
     * SIM_STATE_PUK_REQUIRED     锁定状态，需要用户的PUK码解锁 3
     * SIM_STATE_NETWORK_LOCKED   锁定状态，需要网络的PIN码解锁 4
     * SIM_STATE_READY            就绪状态 5
     */
    int getSimState(Context context);

    /**
     * SIM卡的序列号：
     * 需要权限：READ_PHONE_STATE
     */
    String getSimSerialNumber(Context context);

    /**
     * 服务商名称：
     * 例如：中国移动、联通
     * SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断).
     */
    String getSimOperatorName(Context context);

    /**
     * Returns the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
     * 获取SIM卡提供的移动国家码和移动网络码.5或6位的十进制数字.
     * SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断).
     */
    String getSimOperator(Context context);

    /**
     * Returns the ISO country code equivalent for the SIM provider's country code.
     * 获取ISO国家码，相当于提供SIM卡的国家码。
     */
    String getSimCountryIso(Context context);

    /**
     * 手机类型：
     * 例如： PHONE_TYPE_NONE  无信号
     * PHONE_TYPE_GSM   GSM信号
     * PHONE_TYPE_CDMA  CDMA信号
     */
    int getPhoneType(Context context);

    /**
     * 当前使用的网络类型：
     * 例如： NETWORK_TYPE_UNKNOWN  网络类型未知  0
     * NETWORK_TYPE_GPRS     GPRS网络  1
     * NETWORK_TYPE_EDGE     EDGE网络  2
     * NETWORK_TYPE_UMTS     UMTS网络  3
     * NETWORK_TYPE_HSDPA    HSDPA网络  8
     * NETWORK_TYPE_HSUPA    HSUPA网络  9
     * NETWORK_TYPE_HSPA     HSPA网络  10
     * NETWORK_TYPE_CDMA     CDMA网络,IS95A 或 IS95B.  4
     * NETWORK_TYPE_EVDO_0   EVDO网络, revision 0.  5
     * NETWORK_TYPE_EVDO_A   EVDO网络, revision A.  6
     * NETWORK_TYPE_1xRTT    1xRTT网络  7
     */
    int getNetworkType(Context context);

    /**
     * 手机号：
     * GSM手机的 MSISDN.
     * Return null if it is unavailable.
     */
    String getLine1Number(Context context);

    /**
     * 唯一的设备ID：
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     * Return null if device ID is not available.
     */
    String getIMEI(Context context);

    /**
     * 设备的软件版本号：
     * 例如：the IMEI/SV(software version) for GSM phones.
     * Return null if the software version is not available.
     */
    String getIMEISV(Context context);

    /**
     * 需添加如下权限：
     * <uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     */
    String getIpAddress(Context context);

    /**
     * Returns the WIFI IP Addresses, if any, taking into account IPv4 and IPv6 style addresses.
     *
     * @return the formatted and comma-separated IP addresses, or null if none.
     */
    String getWifiIpAddresses(Context context);

    /**
     * 获取序列号
     */
    String getSerialNumber(Context context);
}
