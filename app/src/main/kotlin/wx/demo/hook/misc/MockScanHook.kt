package wx.demo.hook.misc

import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexMethod
import org.luckypray.dexkit.DexKitBridge

@HookAnno
@ViewAnno
object MockScanHook : SwitchHook("MockScanHook"), IDexFind {
    enum class ScanScene(val source: Int, val a8KeyScene: Int) {
        WECHAT_SCAN(0, 4),// 微信扫一扫识别
        ALBUM_SCAN(1, 34),// 手机相册扫码识别
        LONG_PRESS_SCAN(4, 37)// 长按图片识别
    }

    private object MethodQBarString : DexDescData("MockScanHook.MethodQBarString")

    override val location = "杂项"
    override val funcName = "模拟相机扫码"
    override val funcDesc = "将二维码识别方式模拟成微信相机扫码"

    override fun initOnce() {
        MethodQBarString.toDexMethod {
            hook {
                beforeIfEnabled {
                    val (sourceIndex, a8KeySceneIndex) = if (method.parameterCount == 16) 3 to 4 else 2 to 3
                    val source = args(sourceIndex).int()
                    val a8KeyScene = args(a8KeySceneIndex).int()
                    val matchedScene = ScanScene.entries.find { it.source == source && it.a8KeyScene == a8KeyScene }
                    if (matchedScene == ScanScene.ALBUM_SCAN || matchedScene == ScanScene.LONG_PRESS_SCAN) {
                        args(sourceIndex).set(ScanScene.WECHAT_SCAN.source)
                        args(a8KeySceneIndex).set(ScanScene.WECHAT_SCAN.a8KeyScene)
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodQBarString.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.QBarStringHandler", "key_offline_scan_show_tips")
                }
            }
        }
    }
}
