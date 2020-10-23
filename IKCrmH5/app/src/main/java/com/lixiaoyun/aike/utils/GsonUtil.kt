package com.lixiaoyun.aike.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * @data on 2019/4/28
 */
class GsonUtil private constructor() {
    companion object {
        val instance = SingletonHolder.holder

        val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
    }

    private object SingletonHolder {
        val holder = GsonUtil()
    }

    /**
     * 将对象转成json格式
     *
     * @param object
     * @return String
     */
    fun gsonString(`object`: Any): String? {
        return gson.toJson(`object`)
    }

    /**
     * 转成特定的cls的对象
     *
     * @param gsonString String
     * @param cls Class<T>
     * @return T?
     */
    fun <T> gsonToBean(gsonString: String, cls: Class<T>): T {
        return gson.fromJson(gsonString, cls)
    }

    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    fun <T> gsonToMaps(gsonString: String): Map<String, T> {
        return gson.fromJson(gsonString, object : TypeToken<Map<String, T>>() {

        }.type)
    }

    /**
     * json字符串转成list
     *
     * @param gsonString
     * @param cls
     * @return
     */
    fun <T> GsonToList(gsonString: String, cls: Class<T>): List<T> {
        return gson.fromJson(gsonString, object : TypeToken<List<T>>() {

        }.type)
    }

}