package com.hodaz.goodbyecyword;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Set;

/**
 * Shared Preference를 쉽게 사용하기 위한 Util (메모리 캐싱 기능 포함)
 */
public class PreferenceUtil {

    private static final String TAG = "PreferenceUtil";

    private static PreferenceUtil preferences = new PreferenceUtil();

    public static PreferenceUtil getInstance() {
        return PreferenceUtil.preferences;
    }

    private PreferenceUtil() {
    }

    private HashMap<String, Object> mHashmap = new HashMap<String, Object>();

    public String getString(Context context, String key, String defValue) {
        Object value = mHashmap.get(key);
        if (value == null) {
            SharedPreferences sp = getCommonPreferences(context);
            String s = sp.getString(key, defValue);
            mHashmap.put(key, s);
            return s;
        } else {
            return (String) value;
        }
    }

    public int getInt(Context context, String key, int defValue) {
        Object value = mHashmap.get(key);
        if (value == null) {
            SharedPreferences sp = getCommonPreferences(context);
            Integer s = sp.getInt(key, defValue);
            mHashmap.put(key, s);
            return s;
        } else {
            return (Integer) value;
        }
    }

    public boolean getBoolean(Context context, String key, boolean defValue) {
        Object value = mHashmap.get(key);
        if (value == null) {
            SharedPreferences sp = getCommonPreferences(context);
            Boolean s = sp.getBoolean(key, defValue);
            mHashmap.put(key, s);
            return s;
        } else {
            return (Boolean) value;
        }
    }

    public float getFloat(Context context, String key, float defValue) {
        Object value = mHashmap.get(key);
        if (value == null) {
            SharedPreferences sp = getCommonPreferences(context);
            Float s = sp.getFloat(key, defValue);
            mHashmap.put(key, s);
            return s;
        } else {
            return (Float) value;
        }
    }

    public long getLong(Context context, String key, long defValue) {
        Object value = mHashmap.get(key);
        if (value == null) {
            SharedPreferences sp = getCommonPreferences(context);
            Long s = sp.getLong(key, defValue);
            mHashmap.put(key, s);
            return s;
        } else {
            return (Long) value;
        }
    }

    public String getValue(Context context, String key, String defValue) {
        return getString(context, key, defValue);
    }

    public boolean putString(Context context, String key, String value) {
        try {
            Object putObj = mHashmap.put(key, value);
            String prevValue = putObj == null ? null : (String) putObj;

            if (prevValue == null || (prevValue != null && !prevValue.equals(value))) {
                SharedPreferences sp = getCommonPreferences(context);
                if (sp == null)
                    return false;
                Editor editor = sp.edit();
                editor.putString(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putInt(Context context, String key, int value) {
        try {
            Object putObj = mHashmap.put(key, value);
            Integer prevValue = ( putObj == null || !(putObj instanceof Integer) ) ? null : (Integer) putObj;

            if (putObj == null || prevValue != value) {
                SharedPreferences sp = getCommonPreferences(context);
                if (sp == null)
                    return false;
                Editor editor = sp.edit();
                editor.putInt(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putBoolean(Context context, String key, boolean value) {
        try {
            Object putObj = mHashmap.put(key, value);
            Boolean prevValue = ( putObj == null || !(putObj instanceof Boolean) ) ? null : (Boolean) putObj;

            if (putObj == null || prevValue != value) {
                SharedPreferences sp = getCommonPreferences(context);
                if (sp == null)
                    return false;
                Editor editor = sp.edit();
                editor.putBoolean(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putFloat(Context context, String key, float value) {
        try {
            Object putObj = mHashmap.put(key, value);
            Float prevValue = ( putObj == null || !(putObj instanceof Float) ) ? null : (Float) putObj;

            if (putObj == null || prevValue != value) {
                SharedPreferences sp = getCommonPreferences(context);
                if (sp == null)
                    return false;
                Editor editor = sp.edit();
                editor.putFloat(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putLong(Context context, String key, Long value) {
        try {
            Object putObj = mHashmap.put(key, value);
            Long prevValue = ( putObj == null || !(putObj instanceof Long) ) ? null : (Long) putObj;

            if (putObj == null || prevValue != value) {
                SharedPreferences sp = getCommonPreferences(context);
                if (sp == null)
                    return false;
                Editor editor = sp.edit();
                editor.putLong(key, value);
                editor.commit();
            }
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putStringSet(Context context, String key, Set<String> value) {
        try {
            SharedPreferences sp = getCommonPreferences(context);
            if (sp == null)
                return false;
            Editor editor = sp.edit();
            editor.putStringSet(key, value);
            editor.commit();
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }

    public boolean putValue(Context context, String key, String value) {
        return putString(context, key, value);
    }

    public boolean remove(Context context, String key) {
        try {
            SharedPreferences sp = getCommonPreferences(context);
            if (sp == null)
                return false;

            Editor editor = sp.edit();
            editor.remove(key);
            editor.commit();
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        mHashmap.remove(key);

        return true;
    }

    protected final String PREF_INFO = "common_pref_info";

    private SharedPreferences getCommonPreferences(Context context) {
        SharedPreferences preference = null;
        try {
            if (context == null) {
                return null;
            }
            preference = context.getSharedPreferences(PREF_INFO, Context.MODE_PRIVATE);
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }
        return preference;
    }

    public Set<String> getStringSet(Context context, String key, Set<String> defValue) {
        SharedPreferences sp = getCommonPreferences(context);
        return sp.getStringSet(key, defValue);
    }

    public boolean clearAll(Context context) {
        try {
            SharedPreferences sp = getCommonPreferences(context);
            if (sp == null)
                return false;

            Editor editor = sp.edit();
            editor.clear();
            editor.commit();

            if (mHashmap != null)
                mHashmap.clear();
        } catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return true;
    }
}
