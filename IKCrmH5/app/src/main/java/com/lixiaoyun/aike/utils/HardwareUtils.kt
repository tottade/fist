package com.lixiaoyun.aike.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.lixiaoyun.aike.BuildConfig
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.ContactsBean
import com.lixiaoyun.aike.listener.DownLoadListener
import com.lixiaoyun.aike.network.NetWorkUtil
import com.lixiaoyun.aike.utils.recordingUtils.SalesDynamicsManager
import com.orhanobut.logger.Logger
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.api.widget.Widget
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

/**
 * 硬件相关
 *
 * 打开相机照照片
 * 打开相机照照片
 * 相册选择图片
 * 获取APP版本
 * 获取设备id
 * 获取手机全部联系人
 * 判断是否安装指定的软件
 */
@SuppressLint("MissingPermission")
class HardwareUtils private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = HardwareUtils()
    }

    //高德地图包名
    val Map_GD = "com.autonavi.minimap"
    //百度地图包名
    val Map_BD = "com.baidu.BaiduMap"
    //设备唯一识别id
    val PREFS_DEVICE_ID = "device_id"

    /**
     * 打开相机照照片
     *
     * @param activity Activity
     * @param requestCode Int 请求CODE
     * @return File 照片文件
     */
    fun pickUpCameraTakePhoto(activity: Activity, requestCode: Int = KeySet.REQUEST_TAKE_PHOTO): File {
        val photoFile = AppConfig.AIKE_PHOTO_PATH.getInternalStorageFile()
        val newPhoto = File(photoFile,
                "${DateUtils.instance.getNowString(DateUtils.FORMAT_NO_MODIFICATION)}${AppConfig.AIKE_PHOTO_SUFFIX}.jpg")
        val fileUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, newPhoto)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        activity.startActivityForResult(intent, requestCode)
        return newPhoto
    }

    /**
     * 打开相机照照片
     * @param context Context
     * @param callback (path: String) -> Unit
     */
    fun albumCamera(context: Context, callback: (path: String) -> Unit) {
        Album.camera(context).image().onResult {
            Logger.d("albumCamera Path = $it")
            callback(it)
        }.onCancel {
            Logger.d("Take camera cancel!")
            callback("")
        }.start()
    }

    /**
     * 相册选择图片
     * @param context Context
     * @param widget Widget
     * @param max Int
     * @param camera Boolean
     * @param callback (albumFiles: ArrayList<AlbumFile>) -> Unit
     */
    fun albumGallerySelect(context: Context, widget: Widget = "图片选择".makeWidget(context),
                           max: Int = 1, camera: Boolean = false,
                           callback: (albumFiles: ArrayList<AlbumFile>?) -> Unit) {
        Album.image(context)
                .multipleChoice()
                .widget(widget)
                .camera(camera)
                .columnCount(3)
                .selectCount(max)
                .filterSize {
                    return@filterSize it <= 0
                }
                .afterFilterVisibility(false)
                .onResult { v ->
                    //成功返回
                    callback(v)
                }.onCancel { msg ->
                    Logger.d("albumGallerySelect cancel: $msg")
                    callback(null)
                }.start()
    }

    /**
     * 获取APP版本
     * @param context Context
     * @return String APP版本
     */
    fun getVersionName(context: Context): String {
        val packageManager: PackageManager = context.packageManager
        val packageInfo: PackageInfo = packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }

    /**
     * 获取APP版本(不带v)
     * @param context Context
     * @return String APP版本
     */
    fun getVersionNameWithOutV(context: Context): String {
        return getVersionName(context).replace("v", "")
    }

    /**
     * 获取设备id
     * @return String
     */
    fun getDeviceId(): String {
        var deviceId = SPUtils.instance.getStringSp(PREFS_DEVICE_ID)
        return if (!deviceId.empty()) {
            deviceId!!
        } else {
            val deviceInfo = Build.BOARD + "#" +
                    Build.BRAND + "#" +
                    Build.DEVICE + "#" +
                    Build.DISPLAY + "#" +
                    Build.HOST + "#" +
                    Build.ID + "#" +
                    Build.MANUFACTURER + "#" +
                    Build.MODEL + "#" +
                    Build.PRODUCT + "#" +
                    Build.TAGS + "#" +
                    Build.TYPE + "#" +
                    Build.USER + "#"
            deviceId = UUID.nameUUIDFromBytes(deviceInfo.toByteArray(StandardCharsets.UTF_8)).toString()
            SPUtils.instance.saveValue(PREFS_DEVICE_ID, deviceId)
            return deviceId
        }
    }

    /**
     * 获取屏幕触摸的某个点，是否在某个view之内
     */
    fun comprisePoint(view: View, x: Int, y: Int): Boolean {
        val outRect = Rect()
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        outRect.top = location[1]
        outRect.bottom = outRect.top + view.height
        outRect.left = location[0]
        outRect.right = outRect.left + view.width
        return outRect.contains(x, y)
    }

    /**
     * 获取定位信息
     * @param context Context
     * @param callBack (success: Boolean, location: AMapLocation) -> Unit
     */
    fun getLocation(context: Context, callBack: (success: Boolean, location: AMapLocation) -> Unit) {
        val locationListener = AMapLocationListener {
            if (it != null && it.errorCode == 0) {
                callBack(true, it)
            } else {
                callBack(false, it)
                Logger.d("高德地图定位错误 ErrorCode: ${it.errorCode}, ErrorInfo: ${it.errorInfo}")
            }
        }

        val locationClient = AMapLocationClient(context)
        locationClient.setLocationListener(locationListener)

        val locationOption = AMapLocationClientOption()
        locationOption.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn

        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationOption.isOnceLocation = true
        locationOption.isOnceLocationLatest = true
        locationOption.isNeedAddress = true
        locationOption.httpTimeOut = 12000
        locationOption.isLocationCacheEnable = false

        locationClient.setLocationOption(locationOption)
        locationClient.stopLocation()
        locationClient.startLocation()
    }

    /**
     * 获取手全部联系人
     * @context Context
     * @return ArrayList<ContactsBean>
     */
    @SuppressLint("CheckResult")
    fun getAllContacts(context: Context, callBack: (success: Boolean, dataList: ArrayList<ContactsBean>?) -> Unit) {
        Observable.create<ArrayList<ContactsBean>> {
            try {
                val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val columnName = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                val columnNumber = ContactsContract.CommonDataKinds.Phone.NUMBER
                val data = ArrayList<ContactsBean>()
                val resolver = context.contentResolver
                val cursor = resolver.query(uri, arrayOf(columnName, columnNumber), null, null, null)
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val bean = ContactsBean(cursor.getString(cursor.getColumnIndex(columnName)),
                                cursor.getString(cursor.getColumnIndex(columnNumber)).formatSUH())
                        data.add(bean)
                    }
                    cursor.close()
                }
                it.onNext(data)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            callBack(true, it)
        }, {
            callBack(false, null)
        })
    }

    /**
     * 添加单个联系人
     * @param context Context
     * @param bean ContactsBean
     */
    @SuppressLint("CheckResult")
    fun insertContacts(context: Context, bean: ContactsBean, callBack: (success: Boolean) -> Unit) {
        Observable.create<Boolean> {
            try {
                var uri = ContactsContract.RawContacts.CONTENT_URI
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                //获取raw_contacts的_id
                val rawContactId = ContentUris.parseId(resolver.insert(uri, contentValues))
                //插入data表
                uri = ContactsContract.Data.CONTENT_URI
                //添加 Name
                contentValues.put(ContactsContract.Contacts.Entity.RAW_CONTACT_ID, rawContactId)
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                contentValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, bean.name)
                resolver.insert(uri, contentValues)
                contentValues.clear()
                //添加 Phone
                contentValues.put(ContactsContract.Contacts.Entity.RAW_CONTACT_ID, rawContactId)
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, bean.phone)
                contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                resolver.insert(uri, contentValues)
                contentValues.clear()
                it.onNext(true)
                it.onComplete()
            } catch (e: Exception) {
                Logger.d(e.message ?: "添加联系人异常，确认数据和相关权限正确")
                it.onNext(false)
                it.onComplete()
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callBack)
    }

    /**
     * 批量添加联系人
     * @param context Context
     * @param beanList ArrayList<ContactsBean>
     * @return Boolean
     */
    @SuppressLint("CheckResult")
    fun insertContactsBatch(context: Context, beanList: ArrayList<ContactsBean>, callBack: (success: Boolean) -> Unit) {
        Observable.create<Boolean> {
            try {
                val providerOperation = ArrayList<ContentProviderOperation>()
                var rawContactInsertIndex: Int
                for (contactsBean in beanList) {
                    rawContactInsertIndex = providerOperation.size
                    //添加 Name
                    providerOperation.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .withYieldAllowed(true)
                            .build())
                    providerOperation.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Contacts.Entity.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactsBean.name)
                            .withYieldAllowed(true)
                            .build())
                    //添加 Phone
                    providerOperation.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Contacts.Entity.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactsBean.phone)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .withYieldAllowed(true)
                            .build())
                }
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, providerOperation)
                it.onNext(true)
                it.onComplete()
            } catch (e: Exception) {
                Logger.d(e.message ?: "添加联系人异常，确认数据和相关权限正确")
                it.onNext(false)
                it.onComplete()
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callBack)
    }

    /**
     * 下载文件
     * @param url String
     * @param saveFile File
     * @param downLoadListener DownLoadListener
     */
    fun downLoadFile(url: String, saveFile: File, downLoadListener: DownLoadListener) {
        NetWorkUtil.instance.initRetrofit().download(url)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ResponseBody> {
                    override fun onSubscribe(d: Disposable) {
                        downLoadListener.onStart()
                    }

                    override fun onNext(t: ResponseBody) {
                        //保存下载的录音地址
                        saveRecordPath(t.byteStream(), saveFile) {
                            if (it) {
                                downLoadListener.onSuccess()
                            } else {
                                downLoadListener.onFail("下载失败")
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        downLoadListener.onFail(e.message ?: "下载失败")
                    }

                    override fun onComplete() {

                    }

                })
    }

    /**
     * 流写入文件
     * @param inputStream InputStream
     * @param file File
     *
     * @return File
     */
    fun writeDownFile(inputStream: InputStream, file: File): File {
        if (file.exists()) {
            file.delete()
        }
        val fileReader = ByteArray(4096)
        val outputStream = FileOutputStream(file)
        while (true) {
            val len = inputStream.read(fileReader)
            if (len == -1) {
                break
            }
            outputStream.write(fileReader, 0, len)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
        return file
    }

    /**
     * 保存下载的录音地址
     *
     * @param inputStream InputStream
     * @param file File
     * @param callBack (success: Boolean) -> Unit
     */
    @SuppressLint("CheckResult")
    fun saveRecordPath(inputStream: InputStream, file: File, callBack: (success: Boolean) -> Unit) {
        Observable.create(ObservableOnSubscribe<File> { emitter ->
            emitter.onNext(writeDownFile(inputStream, file))
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    callBack(true)
                    //解析文件并保存地址到本地
                    SalesDynamicsManager.instance.saveCallRecordDir()
                }, {
                    callBack(false)
                })
    }

    /**
     * 判断是否安装指定的软件
     * @param context Context
     * @param packageName String
     * @param callBack (installed: Boolean) -> Unit
     */
    fun isSoftWareInstalled(context: Context, packageName: String, callBack: (installed: Boolean) -> Unit) {
        val packageManager = context.packageManager
        val packageInfoList = packageManager.getInstalledPackages(0)
        val packageNameList = ArrayList<String>()
        for (packageInfo in packageInfoList) {
            val packName = packageInfo.packageName
            packageNameList.add(packName)
        }
        if (packageNameList.contains(packageName)) {
            callBack(true)
        } else {
            callBack(false)
        }
    }
}
