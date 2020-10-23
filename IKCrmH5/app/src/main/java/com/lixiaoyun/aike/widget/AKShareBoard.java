package com.lixiaoyun.aike.widget;

import android.app.Activity;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.lixiaoyun.aike.R;
import com.lixiaoyun.aike.entity.ShareData;
import com.lixiaoyun.aike.utils.ExtraFunsKt;
import com.orhanobut.logger.Logger;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

/**
 * 分享使用 底部的分享平台view
 */
public class AKShareBoard extends PopupWindow implements OnClickListener {

    private Activity mActivity;
    private ShareData mJson;

    public AKShareBoard(Activity activity, ShareData data) {
        super(activity);
        this.mActivity = activity;
        this.mJson = data;
        initView(activity);
    }

    private void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_customer_board, null);
        rootView.findViewById(R.id.wechat).setOnClickListener(this);
        rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
        rootView.findViewById(R.id.qq).setOnClickListener(this);
        rootView.findViewById(R.id.sina).setOnClickListener(this);
        rootView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        setContentView(rootView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_share_view));
        setTouchable(true);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.wechat:
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case R.id.wechat_circle:
                performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case R.id.qq:
                performShare(SHARE_MEDIA.QQ);
                break;
            case R.id.sina:
                performShare(SHARE_MEDIA.SINA);
                break;
            case R.id.btn_cancel:
                this.dismiss();
                break;
            default:
                break;
        }
    }

    private void performShare(SHARE_MEDIA platform) {
        UMWeb umWebShare;
        if (mJson != null) {
            UMImage thumb = new UMImage(mActivity, R.drawable.ic_logo);
            umWebShare = new UMWeb(mJson.getUrl());
            umWebShare.setTitle(mJson.getTitle());
            umWebShare.setDescription(mJson.getContent());
            umWebShare.setThumb(thumb);
            new ShareAction(mActivity)
                    .setPlatform(platform)//传入平台
                    .withMedia(umWebShare)
                    .setCallback(new UMShareListener() {
                        @Override
                        public void onStart(SHARE_MEDIA share_media) {
                            Logger.d("开始分享");
                        }

                        @Override
                        public void onResult(SHARE_MEDIA share_media) {
                            Logger.d("分享完成");
                            dismiss();
                        }

                        @Override
                        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                            Logger.d("分享错误：" + throwable.getMessage());
                            if (throwable.getMessage().contains("没有安装应用")) {
                                ExtraFunsKt.toast("没有安装应用");
                            }
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA share_media) {
                            Logger.d("分享取消");
                        }
                    })//回调监听器
                    .share();

        } else {
            ExtraFunsKt.toast("分享信息未找到");
        }
    }
}
