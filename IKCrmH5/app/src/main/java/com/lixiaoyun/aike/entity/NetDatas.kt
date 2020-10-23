package com.lixiaoyun.aike.entity

import android.os.Parcel
import android.os.Parcelable
import com.lixiaoyun.aike.BuildConfig

/**
 * 网络相关的数据集
 */

/**
 * 登录请求
 */
data class RequestLogin(
        var login: String, var password: String, var client_id: String)

data class RequestLoginWithCorpId(
        var login: String, var password: String, var client_id: String, var corp_id: String
)

/**
 * 登陆返回
 */
data class ResponseLogin(
        val code: Int, val api_message: String, val error: String, val message: String,
        val org_list: List<OrgList>, val data: ResponseLoginData
)

data class OrgList(
        val corp_id: String, val name: String
)

data class ResponseLoginData(
        var user_token: String, var avatar_url: String, var confirmed_phone: Boolean,
        var set_password: Boolean, var fill_user_info: Boolean, var user_id: Int,
        var corp_id: String, var is_expired: Boolean, var api_host: String,
        var crm_app_token: String
)

/**
 * 悬浮窗信息返回
 */
data class ResponseFloatInfo(
        var name: String,
        var company_name: String,
        var entity_id: Int,
        var entity_name: String
)

/**
 * 获取七牛token返回
 */
data class ResponseQiNiuToken(
        var uptoken: String
)

/**
 * 用户信息
 */
data class ResponseUserInfo(
        val avatar_url: String?,
        val department_id: Int,
        val department_name: String?,
        val departments_manager: Boolean,
        val dingtalk_userid: String?,
        val email: String?,
        val enabled_number_hidden_dispose: Boolean,
        val gender: String?,
        val have_new_feature_on_sales_circle: Boolean,
        val id: Int,
        val is_super_user: Boolean,
        val job: String?,
        val locked_at: String?,
        val name: String?,
        val organization_id: Int,
        val organization_name: String?,
        val pending_report: Boolean,
        val phone: String?,
        val role: String?,
        val role_id: Int,
        val sales_circle_msg_count: Int,
        val superior_id: Int,
        val superior_name: String?,
        val tel: String?,
        val uid: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(avatar_url)
        parcel.writeInt(department_id)
        parcel.writeString(department_name)
        parcel.writeByte(if (departments_manager) 1 else 0)
        parcel.writeString(dingtalk_userid)
        parcel.writeString(email)
        parcel.writeByte(if (enabled_number_hidden_dispose) 1 else 0)
        parcel.writeString(gender)
        parcel.writeByte(if (have_new_feature_on_sales_circle) 1 else 0)
        parcel.writeInt(id)
        parcel.writeByte(if (is_super_user) 1 else 0)
        parcel.writeString(job)
        parcel.writeString(locked_at)
        parcel.writeString(name)
        parcel.writeInt(organization_id)
        parcel.writeString(organization_name)
        parcel.writeByte(if (pending_report) 1 else 0)
        parcel.writeString(phone)
        parcel.writeString(role)
        parcel.writeInt(role_id)
        parcel.writeInt(sales_circle_msg_count)
        parcel.writeInt(superior_id)
        parcel.writeString(superior_name)
        parcel.writeString(tel)
        parcel.writeInt(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResponseUserInfo> {
        override fun createFromParcel(parcel: Parcel): ResponseUserInfo {
            return ResponseUserInfo(parcel)
        }

        override fun newArray(size: Int): Array<ResponseUserInfo?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * 生成销售动态
 */
data class ResponseCreateDialLog(
        var dial_log: DialLog
) {
    data class DialLog(var id: Long, var caller_id: Int, var caller_type: String, var user_id: Int,
                       var organization_id: Int, var number: String, var created_at: String,
                       var updated_at: String
    )
}

data class RequestCreateDialog(
        var dial_log: DialLog,
        var audio_ids: Array<String> = arrayOf("0")
) {
    data class DialLog(var call_id: String, var caller_type: String, var caller_id: String, var name: String,
                       var name_type: String, var number: String, var dial_type: String,
                       var number_type: String, var talk_time: String = "0", var is_connected: String = "false"
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestCreateDialog

        if (!audio_ids.contentEquals(other.audio_ids)) return false

        return true
    }

    override fun hashCode(): Int {
        return audio_ids.contentHashCode()
    }
}

/**
 * 更新销售动态
 */
data class ResponseUpDataDialLog(
        var dial_log: DialLog
) {
    data class DialLog(var id: Int, var caller_id: Int, var caller_type: String, var user_id: Int,
                       var organization_id: Int, var number: String, var created_at: String,
                       var updated_at: String
    )
}

data class RequestUpDataDialLog(
        var dial_log: DialLog, var audio_ids: Array<String>
) {
    data class DialLog(
            var talk_time: String, var is_connected: String, var call_id: String
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestUpDataDialLog

        if (!audio_ids.contentEquals(other.audio_ids)) return false

        return true
    }

    override fun hashCode(): Int {
        return audio_ids.contentHashCode()
    }
}

/**
 * 通知消息数据
 */
data class NotificationClickData(
        var data: String?
)

/**
 * 是否开启双卡轮播数据
 */
data class EnableCarouselData(
        var enable_carousel: Boolean?
)

/**
 * 上传推送确认信息
 */
data class PushConfirm(
        var notification_id: Int?,
        var user_id: Int?,
        var app_type: String = if (BuildConfig.IS_LX) "lx_yun" else "aike_yun",
        var device_model: String = android.os.Build.MODEL
)

/**
 * 注册获取短信验证码接口
 */
data class RequestSendOptCode(
        var phone: String,
        var send_type: String,
        var captcha: String
)

/**
 * 注册获取短信验证码接口
 */
data class ResponseBean(
        var code: Int,
        var message: String?,
        var error: String?
)

/**
 * 注册获取短信验证码接口
 */
data class RequestVerificationOptCode(
        var phone: String,
        var code: String
)

/**
 * 注册获取短信验证码接口
 */
data class ResponseVerificationOptCode(
        var user_token: String
)

/**
 * 注册提交注册信息
 */
data class RequestSignUpFillInfo(
        var user_token: String,
        var company_name: String,
        var name: String,
        var job: String,
        var email: String,
        var password: String,
        var ad_source: String = "",
        var brand: String = "lixiao"
)

/**
 * 找回密码验证手机号和验证码
 */
data class ResponseRetrieveVerificationOptCode(
        var user_token: String,
        var user_id: Int
)

/**
 * 修改密码接口
 */
data class RequestChangePassword(
        var phone: String,
        var user_token: String,
        var password: String,
        var password_confirmation: String
)