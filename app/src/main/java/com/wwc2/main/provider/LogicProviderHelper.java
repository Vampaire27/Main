package com.wwc2.main.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.Bundle;

import com.wwc2.common_interface.Provider.ProviderColumns;
import com.wwc2.common_interface.utils.FileUtils;

import java.util.Map.Entry;

/**
 * The helper of the ContentProvider of the main.
 *
 * @author wwc2
 * @date 2017/1/24
 */
public class LogicProviderHelper {
	private static Context mContext = null;
	public static void setContext(Context context) {
		mContext = context;
	}

	private static LogicProviderHelper mLogicProviderHelper = null;
	public static LogicProviderHelper getInstance() {
		if (null == mLogicProviderHelper) {
			mLogicProviderHelper = new LogicProviderHelper(mContext);
		}
		return mLogicProviderHelper;
	}
	
	public static void Provider(String key, String value) {
		Config.mElementMap.put(key, value);
	}
	
	private ContentResolver mContentResolver = null;
	private LogicProviderHelper() {

	}
	
	private LogicProviderHelper(Context context) {
		if (null != context) {
			final String path = context.getApplicationContext().getDatabasePath(Config.DATABASE_NAME).getAbsolutePath();
			FileUtils.deleteFile(path);
			mContentResolver = context.getContentResolver();
		}
	}
	
	public synchronized void start() {
		LogicProvider.notifyChangeMap();
		
		Bundle bundle = new Bundle();
		for (Entry<String, String> entry: Config.mElementMap.entrySet()) {
    		String key = (String)entry.getKey();
    		String value = (String)entry.getValue();
    		if (null != key) {
    			bundle.putString(key, value);
			}
    	}
		update(bundle);
	}

	public void stop() {

	}

	public synchronized void update(Bundle bundle) {
		if (null != mContentResolver) {
			if (null != bundle) {
				ContentValues values = new ContentValues();
				for (Entry<String, String> entry: Config.mElementMap.entrySet()) {
					String key = (String)entry.getKey();
					if (null != key) {
						String string = bundle.getString(key);
						if (null != string) {
							values.put(key, bundle.getString(key));
							break;
						}
					}
				}

				try {
					final Cursor cursor = mContentResolver.query(ProviderColumns.CONTENT_URI, null, null, null, null);
					if (null != cursor) {
						final int count = cursor.getCount();
						if (count > 0) {
							mContentResolver.update(ProviderColumns.CONTENT_URI, values, null, null);
						} else {
							mContentResolver.insert(ProviderColumns.CONTENT_URI, values);
						}
						cursor.close();
					}
				} catch (SQLiteDiskIOException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void update(String key, String value) {
		Bundle providerBundle = new Bundle();
		providerBundle.putString(key, value);
		update(providerBundle);
	}
}
