package wx.demo.hook.flutterbiz

import android.content.ComponentName
import android.content.Intent
import com.highcapable.yukihookapi.hook.param.HookParam
import me.hd.wauxv.data.factory.HostInfo
import me.hd.wauxv.data.factory.WxPlayVersion
import me.hd.wauxv.data.factory.WxVersion
import me.hd.wauxv.data.factory.isAtMost
import me.hd.wauxv.data.factory.isAtMostPlay
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.api.IStartActivity

@HookAnno
@ViewAnno
object NewBizListHook : SwitchHook("NewBizListHook"), IStartActivity {
    override val location = "订阅号"
    override val funcName = "订阅消息列表"
    override val funcDesc = "订阅号消息从瀑布流模式改为列表模式"
    override val isAvailable = isAtMost(WxVersion.V8_0_64) || isAtMostPlay(WxPlayVersion.V8_0_64_PLAY_2922)

    override fun initOnce() {
    }

    override fun onStartActivityIntent(param: HookParam, intent: Intent) {
        if (!isEnabled) return
        if (!isAvailable) return
        when (intent.component?.className) {
            "com.tencent.mm.plugin.brandservice.ui.flutter.BizFlutterTLFlutterViewActivity",
            "com.tencent.mm.plugin.brandservice.ui.timeline.BizTimeLineUI" -> {
                intent.component = ComponentName(HostInfo.appPackageName, "com.tencent.mm.ui.conversation.NewBizConversationUI")
            }
        }
    }
}
