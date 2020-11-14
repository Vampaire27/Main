package com.wwc2.main.upgrade.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by swd1 on 17-6-1.
 */

public class NetworkUtils {
    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivity.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }

            }
        }

        return false;
    }

    public static String getWebContent(String url) {
        HttpURLConnection connection;
        try {
            // 创建一个URL对象并传入地址
            URL mUrl = new URL(url);
            // 用地址打开连接通道
            connection = (HttpURLConnection) mUrl.openConnection();
            // 设置请求方式为get
            connection.setRequestMethod("GET");
            // 设置连接超时为8秒
            connection.setConnectTimeout(8000);
            // 设置读取超时为8秒
            connection.setReadTimeout(8000);
//            // 设置可取
//            connection.setDoInput(true);
//            // 设置可读
//            connection.setDoOutput(true);
            // 得到输入流
            InputStream in = connection.getInputStream();
            // 创建高效流对象
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            // 创建StringBuilder对象存储数据
            StringBuilder response = new StringBuilder();
            String line;// 一次读取一行
            while ((line = reader.readLine()) != null) {
                response.append(line);// 得到的数据存入StringBuilder
            }
            return response.toString();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

