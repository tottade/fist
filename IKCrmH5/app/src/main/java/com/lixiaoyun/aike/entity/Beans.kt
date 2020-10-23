package com.lixiaoyun.aike.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lixiaoyun.aike.listener.OnSheetItemClickListener
import com.lixiaoyun.aike.network.NetWorkConfig
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.socketUtils.RxWebSocket
import okhttp3.WebSocket
import okio.ByteString
import java.util.*

/**
 * Beans数据集
 */

/**
 * 添加日程事件
 *
 * @property title String 事件标题
 * @property description String 事件描述
 * @property location String 事件发生地址
 * @property beginTime String 事件开始时间 yyyy-MM-dd HH:mm
 * @property endTime String 事件结束时间 yyyy-MM-dd HH:mm
 * @property remindTime Int 提前提醒(秒)
 */
data class AddCalendarEventBean(
        var title: String,
        var description: String,
        var location: String,
        var beginTime: String,
        var endTime: String,
        var remindTime: Int = 15 * 60)

/**
 * 图片预览
 *
 * @property urls List<String> 连接集合
 * @property current String
 * @constructor
 */
data class PreviewPhotosBean(var urls: ArrayList<String>?, var current: String?)
    : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createStringArrayList(),
            parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeStringList(urls)
        writeString(current)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<PreviewPhotosBean> {
        override fun createFromParcel(parcel: Parcel): PreviewPhotosBean = PreviewPhotosBean(parcel)
        override fun newArray(size: Int): Array<PreviewPhotosBean?> = arrayOfNulls(size)
    }
}

/**
 * 底部弹出栏
 * @property name String
 * @property onItemClickListener OnSheetItemClickListener
 * @property color Int
 * @constructor
 */
data class SheetItem(
        var name: String,
        var onItemClickListener: OnSheetItemClickListener,
        var color: Int)

/**
 * 获取设备信息
 * @property clientId String
 * @property simAmount Int      sim卡数量
 * @constructor
 */
data class PhoneInfoData(
        var clientId: String = "",
        var simAmount: Int = 0
)

/**
 * title
 * @property title String
 * @constructor
 */
data class SetTitleData(
        var title: String //我是一个标题
)

/**
 * 左右按钮
 * @property show Boolean
 * @property control Boolean
 * @property text String
 * @property num Int
 * @constructor
 */
data class SetLeftRight(
        var show: Boolean, //false
        var control: Boolean, //true
        var text: String, //发送
        var num: Int
)

/**
 * 菜单按钮
 * @property backgroundColor String
 * @property textColor String
 * @property items List<Item>
 * @constructor
 */
data class SetMenuData(
        var backgroundColor: String, //#ADD8E6
        var textColor: String, //#ADD8E611
        var items: List<Item>
)

data class SetMenuPicData(
        var show: Boolean, //false
        var backgroundColor: String, //#ADD8E6
        var textColor: String, //#ADD8E611
        var count: Int, // 11
        var items: List<Item>
)

data class Item(
        var id: String, //1
        var iconId: String, //file
        var text: String //帮助
)

/**
 * 打开连接
 * @property url String
 * @constructor
 */
data class OpenLinkData(
        var url: String //http://www.ikcrm.com
)

/**
 * 日期选择器
 * @property format String
 * @property value String
 * @constructor
 */
data class TimePickerData(
        var format: String, //yyyy-MM-dd HH:mm
        var value: String //2018-02-02 17:30
)

/**
 * 上传图库图片
 * @property compression Boolean
 * @property multiple Boolean
 * @property max Int
 * @property quality Int
 * @property resize Int
 * @constructor
 */
data class AlbumData(
        var compression: Boolean, //true 是否压缩
        var multiple: Boolean, //false 单张？：多张
        var max: Int, //3 最大数量
        var quality: Int, //50 图片质量
        var resize: Int //50
)

/**
 * 拍照上传
 * @property compression Boolean
 * @property quality Int
 * @property resize Int
 * @property stickers Stickers
 * @constructor
 */
data class UploadImageFromCamera(
        val compression: Boolean?,
        val quality: Int?,
        val resize: Int?,
        var stickers: Stickers?
)

data class Stickers(
        val address: String,
        val dateWeather: String,
        val time: String,
        val username: String
)

/**
 * 文件预览返回
 *
 * @property url String
 * @property name String
 * @property size Long
 * @constructor
 */
data class PreviewFileData(var url: String?,
                           var name: String?,
                           var size: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readLong()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(url)
        writeString(name)
        writeLong(size)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<PreviewFileData> {
        override fun createFromParcel(parcel: Parcel): PreviewFileData {
            return PreviewFileData(parcel)
        }

        override fun newArray(size: Int): Array<PreviewFileData?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 扫码返回
 * */
data class SendScanCode(
        var data: String? //扫码
)

/**
 * 分享
 *
 * @property type Int
 * @property url String
 * @property title String
 * @property content String
 * @property image String
 * @constructor
 */
data class ShareData(
        var type: Int, //0
        var url: String, //http://www.ikcrm.com
        var title: String, //分享标题
        var content: String, //分享内
        var image: String //https://i01.lw.aliimg.com/tfs/TB1TUovHXXXXXbCXpXXNC1IYXXXLAIWANGi_1_120_120.jpg
)

/**
 * SharedPreferences
 *
 * @property key String
 * @property value String
 * @constructor
 */
data class SPData(
        var key: String,
        var value: String
)

/**
 * 定位信息
 * @property coordinate Double
 * @property targetAccuracy Double
 * @property useCache Boolean
 * @property withReGeocode Boolean
 * @constructor
 */
data class LocationData(
        val coordinate: Double,
        val targetAccuracy: Double,
        val useCache: Boolean,
        val withReGeocode: Boolean
)

/**
 * 定位信息返回
 * @property longitude Double
 * @property latitude Double
 * @constructor
 */
data class LocationCallBack(
        val longitude: Double,
        val latitude: Double
)

/**
 * 给H5上传数据bean
 *
 * @property device String
 * @property user HybridAppConfigUserBean
 * @property org HybridAppConfigOrgBean
 * @property api HybridAppConfigApiBean
 * @property features UserFeatures
 * @constructor
 */
data class HybridAppConfigBean(
        var device: String? = "android",
        var appToken: String? = null,
        var version_code: String? = NetWorkConfig.VERSION_CODE_WITHOUT_V,
        var user: HybridAppConfigUserBean? = null,
        var org: HybridAppConfigOrgBean? = null,
        var api: HybridAppConfigApiBean? = null,
        var features: UserFeatures? = null
)

data class HybridAppConfigUserBean(
        var id: String? = null,
        var name: String? = null,
        var token: String? = null,
        var phone: String? = null,
        var isSuperUser: Boolean? = false,
        var avatar: String? = null,
        var status: String? = null,
        var client_id: String? = null,
        var login: String? = null,
        var password: String? = null,
        var device_model: String? = null,
        var system_version: String? = null,
        var device_id: String? = null,
        var platform: String? = null,
        var uid: String? = null
)

data class HybridAppConfigOrgBean(
        var id: String? = null,
        var corpId: String? = null,
        var expiresAt: String? = null
)

data class HybridAppConfigApiBean(
        var domain: String? = null,
        var pathPrefix: String? = "/api/v2"
)

data class UserFeatures(
        var callCenter: Boolean = false
)

/**
 * pushType = 11 解析的数据
 * PC调起拨号
 */
data class PushAppDialBean(
        var call_id: String,
        var action: Int,
        var user_id: Int,
        var push_type: Int,
        var start_time: String,
        var number: String,
        var name_type: String,
        var caller_id: Int,
        var caller_type: String,
        val middle_call_number: String,
        val enable_carousel: Boolean = false,
        val carousel_card_no: Int = 0
) {
    fun checkEmpty(): Boolean {
        return user_id == 0 || caller_id == 0 || (name_type.empty() && middle_call_number.empty()) || number.empty() || start_time.empty() || caller_type.empty()
    }
}

/**
 * 录音路径信息
 * @property version String
 * @property date String
 * @property path List<PathBean>
 * @constructor
 */
data class RecordPathBean(
        var version: String,
        var date: String,
        var path: List<PathBean>
) {
    data class PathBean(
            var rom: String,
            var display: List<DisplayBean>
    ) {
        data class DisplayBean(
                var path: String
        )
    }
}

/**
 * 返回H5数据
 */
data class WebBackBean(val message: String, val code: Int)

data class WebBackSuccess(val success: WebBackBean)

data class WebBackError(val error: WebBackBean)

/**
 * 上传七牛返回bean
 * @property bucket String
 * @property duration Double
 * @property etag String
 * @property file_url String
 * @property id Int
 * @property key String
 * @property name String
 * @property persistentId String
 * @property size Int
 * @property type String
 * @constructor
 */
data class QiNiuResponse(
        val bucket: String,
        val duration: Double,
        val etag: String,
        val file_url: String,
        val id: Int,
        val key: String,
        val name: String,
        val persistentId: String,
        val size: Int,
        val type: String
)

/**
 * 七牛错误返回
 * @property callbackBody String
 * @property callbackBodyType String
 * @property callbackFetchKey Int
 * @property callbackHost String
 * @property callbackUrl String
 * @property errCode Int
 * @property error String
 * @property hash String
 * @property key String
 * @property token String
 * @constructor
 */
data class QiNiuErrorResponse(
        @SerializedName("callback_body")
        val callbackBody: String = "",
        @SerializedName("callback_bodyType")
        val callbackBodyType: String = "",
        @SerializedName("callback_fetchKey")
        val callbackFetchKey: Int = 0,
        @SerializedName("callback_host")
        val callbackHost: String = "",
        @SerializedName("callback_url")
        val callbackUrl: String = "",
        @SerializedName("err_code")
        val errCode: Int = 0,
        @SerializedName("error")
        val error: String = "",
        @SerializedName("hash")
        val hash: String = "",
        @SerializedName("key")
        val key: String = "",
        @SerializedName("token")
        val token: String = ""
)

/**
 * 上传图片bean
 * @property id Int
 * @property url String
 * @constructor
 */
data class UploadImageBean(
        val id: Int,
        val url: String
)

/**
 * 上传手动录音bean
 *
 * @property id Int
 * @property file_url String
 * @property duration String
 * @constructor
 */
data class UploadRecordBean(
        val id: Int,
        val file_url: String,
        val duration: String
)

/**
 * 播放录音bean
 * @property url String
 * @constructor
 */
data class PlayAudioBean(var url: String)

/**
 * 联系人Bean
 * @property name String
 * @property phone String
 * @constructor
 */
data class ContactsBean(var name: String, var phone: String)

/**
 * 推送传递过来的数据
 * @property category Int
 * @property created_at Int
 * @property extras Extras
 * @property id Any
 * @property is_ring Boolean
 * @property is_vibrate Boolean
 * @property notifiable_id Any
 * @property notifiable_type String
 * @property notify_type String
 * @property push_type Int
 * @property subject_id String
 * @property subject_type String
 * @property text String
 * @property title String
 * @property user_id Int
 * @constructor
 */
data class PushDataBean(
        val category: Int,
        val created_at: Int,
        val extras: Extras,
        val id: Int,
        val is_ring: Boolean,
        val is_vibrate: Boolean,
        val notifiable_id: Int,
        val notifiable_type: String,
        val notify_type: String,
        val push_type: Int,
        val subject_id: String,
        val subject_type: String,
        val text: String,
        val title: String,
        val user_id: Int
) {
    data class Extras(
            val text: String,
            val title: String
    )
}

//打电话bean
data class CallPhoneBean(
        val call_id: String,
        val name: String,
        val name_type: String,
        val caller_id: String,
        val caller_type: String,
        val companyName: String,
        val number: String,
        val middle_call_number: String,
        val enable_carousel: Boolean = false,
        val carousel_card_no: String
)

//获取阿里云token
data class AliyunTokenBean(
        var StatusCode: String? = "",
        var AccessKeyId: String? = "",
        var AccessKeySecret: String? = "",
        var SecurityToken: String? = "",
        var Expiration: String? = ""
)

//阿里云log
data class AliyunLogEntity(
        var __topic__: String? = "",
        var timestamp: String? = "",
        var app_version: String? = "",
        var os_version: String? = "",
        var device_type: String? = "",
        var user_id: Int = 0,
        var user_phonenumber: String? = "",
        var event_type: String? = "",
        var request_api: String? = "",
        var request_params: String? = "",
        var response_params: String? = "",
        var request_time: String? = "",
        var event_message: String? = ""
)

/**
 * 当前websocket信息
 *
 * @property url String?
 * @property webSocket WebSocket?
 * @property msg String?
 * @property msgByteString ByteString?
 * @property isConnect Boolean
 * @property isReconnect Boolean
 * @property isPrepareReconnect Boolean
 * @property status RxWebSocketStatus
 * @constructor
 */
data class WebSocketInfo(
        var url: String? = "",
        var webSocket: WebSocket? = null,
        var msg: String? = "",
        var msgByteString: ByteString? = null,
        var isConnect: Boolean = false,
        var isReconnect: Boolean = false,
        var isPrepareReconnect: Boolean = false,
        var status: RxWebSocket.RxWebSocketStatus = RxWebSocket.RxWebSocketStatus.CLOSED
) {

    /**
     * 重置
     */
    fun reset(): WebSocketInfo {
        webSocket = null
        msg = null
        msgByteString = null
        isConnect = false
        isReconnect = false
        isPrepareReconnect = false
        status = RxWebSocket.RxWebSocketStatus.CLOSED
        return this
    }
}

/**
 * websocket通信bean
 *
 * @property cmd Int?
 * @property msg DialMsgBean?
 * @property msgId String?
 * @constructor
 */
data class WebSocketMsg(
        var cmd: Int? = 1,
        var msg: DialMsgBean? = null,
        var msgId: String? = null
) {
    data class DialMsgBean(
            var call_id: String,
            var action: Int,
            var user_id: Int,
            var push_type: Int,
            var start_time: String,
            var number: String,
            val middle_call_number: String,
            var name_type: String,
            var caller_id: Int,
            var caller_type: String,
            val enable_carousel: Boolean = false,
            var carousel_card_no: Int = 0
    )
}

data class TipsViewData(
        var maxLength: Int,
        var minLength: Int,
        var inputHint: String,
        var tipsMsg: String,
        var isTipsMsgShow: Boolean,
        var errorTips: String,
        var showImgTips: Boolean,
        var required: Boolean,
        var editLimit: String
)