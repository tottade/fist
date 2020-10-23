package com.lixiaoyun.aike.utils

import android.app.Activity
import android.content.SharedPreferences
import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.constant.AppConfig

class SPUtils private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = SPUtils()
    }

    private val sp: SharedPreferences = AKApplication.instance.getSharedPreferences(AppConfig.DEF_SP_NAME, Activity.MODE_MULTI_PROCESS)

    fun saveValue(key: String, value: Any): Boolean {
        return when (value) {
            is String -> {
                return sp.edit().putString(key, value).commit()
            }
            is Boolean -> {
                return sp.edit().putBoolean(key, value).commit()
            }
            is Int -> {
                return sp.edit().putInt(key, value).commit()
            }
            is Double -> {
                return sp.edit().putFloat(key, value.toFloat()).commit()
            }
            is Float -> {
                return sp.edit().putFloat(key, value.toFloat()).commit()
            }
            is Long -> {
                return sp.edit().putLong(key, value).commit()
            }
            else -> {
                "存储数据类型不匹配".toast()
                false
            }
        }
    }

    fun saveH5Value(key: String, value: String?): Boolean {
        return if (value.empty()) {
            false
        } else {
            sp.edit().putString(key, value).commit()
        }
    }

    fun getStringSp(key: String, defValue: String = ""): String? {
        return sp.getString(key, defValue)
    }

    fun getIntSp(key: String, defValue: Int = 0): Int? {
        return sp.getInt(key, defValue)
    }

    fun getBoolSp(key: String, defValue: Boolean = false): Boolean? {
        return sp.getBoolean(key, defValue)
    }

    fun getFloat(key: String, defValue: Float): Float? {
        return sp.getFloat(key, defValue)
    }

    fun getLong(key: String, defValue: Long = 0): Long? {
        return sp.getLong(key, defValue)
    }

    fun clear(key: String): Boolean {
        return sp.edit().remove(key).commit()
    }

    fun clearAll(): Boolean {
        return sp.edit().clear().commit()
    }

    fun remove(key: String?): Boolean {
        return if (key.empty()) {
            false
        } else {
            sp.edit().remove(key).commit()
        }
    }
}