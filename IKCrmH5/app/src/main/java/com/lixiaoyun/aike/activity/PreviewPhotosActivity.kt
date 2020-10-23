package com.lixiaoyun.aike.activity

import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.os.Bundle
import android.view.KeyEvent
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.lixiaoyun.aike.R
import com.lixiaoyun.aike.adapter.PreviewPhotosAdapter
import com.lixiaoyun.aike.constant.KeySet
import com.lixiaoyun.aike.entity.PreviewPhotosBean
import com.lixiaoyun.aike.utils.StatusBarUtil
import com.lixiaoyun.aike.utils.empty
import com.lixiaoyun.aike.utils.toast
import com.lixiaoyun.aike.widget.AKTitleBar
import kotlinx.android.synthetic.main.activity_preview_photos.*

/**
 * viewpager 预览多图，支持手势缩放
 */
class PreviewPhotosActivity : BaseActivity() {

    companion object {
        fun intentToPreviewPhotosActivity(context: Context, bean: PreviewPhotosBean) {
            //检查urls不为空，当前显示页包含于urls中
            if (bean.urls!!.isNotEmpty()) {
                if (bean.current.empty()) {
                    bean.current = bean.urls!![0]
                    val intent = Intent(context, PreviewPhotosActivity::class.java)
                    intent.putExtra(KeySet.I_PREVIEW_PHOTOS_DATA, bean)
                    context.startActivity(intent)
                } else {
                    if (bean.urls!!.contains(bean.current)) {
                        val intent = Intent(context, PreviewPhotosActivity::class.java)
                        intent.putExtra(KeySet.I_PREVIEW_PHOTOS_DATA, bean)
                        context.startActivity(intent)
                    } else {
                        "获取图片信息有误".toast()
                    }
                }
            } else {
                "获取图片信息有误".toast()
            }
        }
    }

    var canReset = false//控制当前页未切换时，不重置图像

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_photos)
        StatusBarUtil.setImmersiveStatusBar(this, root, true, false, getColor(R.color.colorPrimary))

        val bean = intent.getParcelableExtra<PreviewPhotosBean>(KeySet.I_PREVIEW_PHOTOS_DATA)
                ?: return

        vAkTitleBar.setLeftClick(object : AKTitleBar.ClickListener {
            override fun onClick() {
                finish()
            }
        })

        setTitleIndex(1, bean.urls!!.size)
        val currentItemId = bean.urls!!.indexOf(bean.current)

        vViewpager.adapter = PreviewPhotosAdapter(bean.urls!!)
        vViewpager.setCurrentItem(currentItemId, false)
        vViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE && canReset) {
                    val childCount = vViewpager.childCount
                    // 当图片滑动到下一页后，遍历当前所有加载过的PhotoView，恢复所有图片的默认状态大小
                    for (i in 0 until childCount) {
                        val childAt = vViewpager.getChildAt(i)
                        if (childAt != null && childAt is PhotoView) {
                            // 把得到的photoView放到这个负责变形的类当中
                            val attach = PhotoViewAttacher(childAt)
                            attach.setDisplayMatrix(Matrix())
                        }
                    }
                    canReset = false
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                setTitleIndex(position + 1, bean.urls!!.size)
                canReset = true
            }

        })
    }

    private fun setTitleIndex(index: Int, max: Int) {
        vAkTitleBar.setCenterText("$index/$max")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
