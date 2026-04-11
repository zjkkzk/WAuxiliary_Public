package wx.demo.hook.chat

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
object DisableSendStatusHook : SwitchHook("DisableSendStatusHook"), IDexFind {
    private object MethodDoDirectSend : DexDescData("DisableSendStatusHook.MethodDoDirectSend")

    override val location = "聊天"
    override val funcName = "禁止发送状态"
    override val funcDesc = "禁止聊天框文本改变时发送正在输入中"

    override fun initOnce() {
        MethodDoDirectSend.toDexMethod {
            hook {
                beforeIfEnabled {
                    resultNull()
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodDoDirectSend.findDexClassMethod(dexKit) {
            onMethod {
                searchPackages("com.tencent.mm.ui.chatting.component")
                matcher {
                    usingEqStrings("MicroMsg.SignallingComponent", "[doDirectSend] mChattingContext is null!")
                }
            }
        }
    }
}
