package wx.demo.hook.chat

import android.widget.Button
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.data.factory.WxPlayVersion
import me.hd.wauxv.data.factory.WxVersion
import me.hd.wauxv.data.factory.isAtLeast
import me.hd.wauxv.data.factory.isAtLeastPlay
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexMethod
import org.luckypray.dexkit.DexKitBridge

@HookAnno
@ViewAnno
object AutoViewOriginalPhotoHook : SwitchHook("AutoViewOriginalPhotoHook"), IDexFind {
    private object MethodSetImageHdImgBtnVisibility : DexDescData("AutoViewOriginalPhotoHook.MethodSetImageHdImgBtnVisibility")
    private object MethodCheckNeedShowOriginVideoBtn : DexDescData("AutoViewOriginalPhotoHook.MethodCheckNeedShowOriginVideoBtn")

    override val location = "聊天"
    override val funcName = "自动查看原图"
    override val funcDesc = "在打开图片和视频时自动点击查看原图"

    override fun initOnce() {
        listOf(MethodSetImageHdImgBtnVisibility, MethodCheckNeedShowOriginVideoBtn).forEach { descData ->
            descData.toDexMethod {
                hook {
                    afterIfEnabled {
                        instance.asResolver().field {
                            type = Button::class
                        }.forEach {
                            it.get<Button>()?.let { imgBtn ->
                                if (imgBtn.isVisible) {
                                    val keywords = listOf(
                                        "查看原图", "Full Image",
                                        "查看原视频", "Original quality",
                                    )
                                    if (keywords.any { text -> imgBtn.text.contains(text, ignoreCase = true) }) {
                                        imgBtn.performClick()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodSetImageHdImgBtnVisibility.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    declaredClass = "com.tencent.mm.ui.chatting.gallery.ImageGalleryUI"
                    if (isAtLeast(WxVersion.V8_0_54) || isAtLeastPlay(WxPlayVersion.V8_0_54_PLAY_2740)) {
                        usingEqStrings("setHdImageActionDownloadable")
                    } else {
                        usingEqStrings("setImageHdImgBtnVisibility")
                    }
                }
            }
        }
        MethodCheckNeedShowOriginVideoBtn.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    declaredClass = "com.tencent.mm.ui.chatting.gallery.ImageGalleryUI"
                    usingEqStrings("checkNeedShowOriginVideoBtn")
                }
            }
        }
    }
}
