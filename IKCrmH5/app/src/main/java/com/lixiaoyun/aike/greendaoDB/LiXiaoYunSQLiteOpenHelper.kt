package com.lixiaoyun.aike.greendaoDB

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.github.yuweiguocn.library.greendao.MigrationHelper
import org.greenrobot.greendao.database.Database

/**
 * @data on 2019/8/5
 */
class LiXiaoYunSQLiteOpenHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?) : DaoMaster.OpenHelper(context, name, factory) {

    override fun onUpgrade(db: Database?, oldVersion: Int, newVersion: Int) {

        MigrationHelper.migrate(db, object : MigrationHelper.ReCreateAllTableListener {
            override fun onDropAllTables(db: Database?, ifExists: Boolean) {
                DaoMaster.dropAllTables(db, ifExists)

            }

            override fun onCreateAllTables(db: Database?, ifNotExists: Boolean) {
                DaoMaster.createAllTables(db, ifNotExists)
            }
        }, SalesDynamicsModelDao::class.java)
    }
}