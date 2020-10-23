package com.lixiaoyun.aike.utils

import com.lixiaoyun.aike.AKApplication
import com.lixiaoyun.aike.greendaoDB.DaoMaster
import com.lixiaoyun.aike.greendaoDB.DaoSession
import com.lixiaoyun.aike.greendaoDB.LiXiaoYunSQLiteOpenHelper
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.query.WhereCondition

/**
 * @data on 2019/5/29
 */
class DBUtils private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = DBUtils()
    }

    private val devOpenHelper = LiXiaoYunSQLiteOpenHelper(AKApplication.instance, "LiXiaoYun.db", null)
    private val daoMaster = DaoMaster(devOpenHelper.writableDatabase)
    val dBOperator: DaoSession = daoMaster.newSession()

    /**
     * 新增数据
     *
     * @param dao AbstractDao<T, Long>
     * @param data T
     */
    fun <T> insertData(dao: AbstractDao<T, Long>, data: T) {
        dao.insertOrReplace(data)
    }

    /**
     * 查询数据
     * @param dao AbstractDao<T, Long>
     * @param condition WhereCondition
     * @return T
     */
    fun <T> getDataByAnd(dao: AbstractDao<T, Long>, condition: WhereCondition): T {
        return dao.queryBuilder().where(condition).unique()
    }

    fun <T> getDataListByAnd(dao: AbstractDao<T, Long>, condition: WhereCondition): List<T> {
        return dao.queryBuilder().where(condition).list()
    }

    /**
     * 删除数据
     * @param dao AbstractDao<T, Long>
     * @param data T
     */
    fun <T> deleteData(dao: AbstractDao<T, Long>, data: T) {
        dao.delete(data)
    }

    fun <T> deleteDataByKey(dao: AbstractDao<T, Long>, primaryKey: Long) {
        dao.deleteByKey(primaryKey)
    }

}