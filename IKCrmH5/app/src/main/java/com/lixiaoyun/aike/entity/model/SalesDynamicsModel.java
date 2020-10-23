package com.lixiaoyun.aike.entity.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * @data on 2019/5/29
 */

@Entity
public class SalesDynamicsModel {

    //编号 设置自增长
    @Id(autoincrement = true)
    private Long id;
    //通话条目id
    private Long itemId;
    //通话条目call_id
    private String callId;
    //通话开始时间
    private Long createTime;
    //通话结束时间
    private Long endTime;
    //拨打电话的用户名
    private String name;
    //电话号码
    private String phoneNumber;
    //拨打电话的方式
    private String phoneType;
    //通话时长
    private int duration;
    //保存在手机上的别名
    private String contactAlias;
    //电话录音路径
    private String recordFilePath;
    //拨打电话的模块名称
    private String callerType;
    //拨打电话的模块Id
    private String callerId;
    //拨打电话的模块名称(只有在客户下的联系人拨号时才会不一样)
    private String nameType;
    //电话是否挂断
    private boolean takeOff = false;

    @Generated(hash = 604011000)
    public SalesDynamicsModel(Long id, Long itemId, String callId, Long createTime,
                              Long endTime, String name, String phoneNumber, String phoneType,
                              int duration, String contactAlias, String recordFilePath,
                              String callerType, String callerId, String nameType, boolean takeOff) {
        this.id = id;
        this.itemId = itemId;
        this.callId = callId;
        this.createTime = createTime;
        this.endTime = endTime;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.phoneType = phoneType;
        this.duration = duration;
        this.contactAlias = contactAlias;
        this.recordFilePath = recordFilePath;
        this.callerType = callerType;
        this.callerId = callerId;
        this.nameType = nameType;
        this.takeOff = takeOff;
    }

    @Generated(hash = 2071131387)
    public SalesDynamicsModel() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getCallId() {
        return this.callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneType() {
        return this.phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getContactAlias() {
        return this.contactAlias;
    }

    public void setContactAlias(String contactAlias) {
        this.contactAlias = contactAlias;
    }

    public String getRecordFilePath() {
        return this.recordFilePath;
    }

    public void setRecordFilePath(String recordFilePath) {
        this.recordFilePath = recordFilePath;
    }

    public String getCallerType() {
        return this.callerType;
    }

    public void setCallerType(String callerType) {
        this.callerType = callerType;
    }

    public String getCallerId() {
        return this.callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getNameType() {
        return this.nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }

    public boolean getTakeOff() {
        return this.takeOff;
    }

    public void setTakeOff(boolean takeOff) {
        this.takeOff = takeOff;
    }
}
