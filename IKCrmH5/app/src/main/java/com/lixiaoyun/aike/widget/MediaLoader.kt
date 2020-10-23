package com.lixiaoyun.aike.widget

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.lixiaoyun.aike.R
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.AlbumLoader

/**
 * @data on 2019/5/7
 *
 * 自定义图片选择器Loader
 */
class MediaLoader : AlbumLoader {
    override fun load(imageView: ImageView?, albumFile: AlbumFile?) {
        load(imageView, albumFile?.path)
    }

    override fun load(imageView: ImageView?, url: String?) {
        Glide.with(imageView!!.context)
                .load(url)
                .error(R.drawable.bg_img_error)
                .transition(DrawableTransitionOptions.with(
                        DrawableCrossFadeFactory
                                .Builder(300)
                                .setCrossFadeEnabled(true)
                                .build())
                ).into(imageView)
    }
}