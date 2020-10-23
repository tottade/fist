package com.lixiaoyun.aike.adapter

import android.graphics.drawable.Drawable
import androidx.viewpager.widget.PagerAdapter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.lixiaoyun.aike.R
import com.orhanobut.logger.Logger

class PreviewPhotosAdapter(private val urls: List<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val root = LinearLayout(container.context)
        root.gravity = Gravity.CENTER

        val photoView = PhotoView(container.context)
        val photoViewLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        Glide.with(container.context)
                .load(urls[position])
                .placeholder(R.drawable.bg_img_placeholder)
                .error(R.drawable.bg_img_error)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Logger.e("${urls[position]} onLoadFailed")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Logger.i("${urls[position]} onResourceReady")
                        return false
                    }
                })
                .into(photoView)

        photoView.layoutParams = photoViewLp
        root.addView(photoView)

        container.addView(root)

        return root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun getCount(): Int {
        return if (urls.isNotEmpty()) urls.size else 0
    }

}