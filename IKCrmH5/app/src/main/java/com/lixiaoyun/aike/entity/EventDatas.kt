package com.lixiaoyun.aike.entity

import com.lixiaoyun.aike.network.NetStateMonitor

/**
 * EventBus数据集
 */

/**
 * 蓝牙相关
 *
 * @property name String
 * @property type String
 * @property code String
 * @property address String
 * @constructor
 */
data class BluetoothEvent(var name: String, var type: String, var code: String, var address: String)

/**
 * 网路变化
 *
 * @property netState NetState
 * @constructor
 */
data class NetWorkStateEvent(var netState: NetStateMonitor.NetState)

/**
 * 录音相关
 *
 * @property recordPath String
 * @constructor
 */
data class RecordEvent(var recordPath: String)