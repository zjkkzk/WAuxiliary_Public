package wx.demo.hook.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.databinding.ModuleDialogDisableRingtonePlayBinding
import me.hd.wauxv.ui.setting.factory.showConfigDialog
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexMethod
import org.luckypray.dexkit.DexKitBridge

@HookAnno
@ViewAnno
object DisableRingtonePlayHook : SwitchHook("DisablePlayRingtoneHook"), IDexFind {
    private object MethodPlaySound : DexDescData("DisablePlayRingtoneHook.MethodPlaySound")
    private object ValDisOutCall : DefaultData("DisablePlayRingtoneHook.ValDisOutCall", booleanDefVal = true)
    private object ValDisInCall : DefaultData("DisablePlayRingtoneHook.ValDisInCall", booleanDefVal = false)

    override val location = "聊天"
    override val funcName = "屏蔽通话铃声"
    override val funcDesc = "屏蔽视频及语音通话时呼出呼入的铃声"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogDisableRingtonePlayBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogCbDisableRingtoneOutCall.isChecked = ValDisOutCall.booleanVal
        binding.moduleDialogCbDisableRingtoneInCall.isChecked = ValDisInCall.booleanVal
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValDisOutCall.booleanVal = binding.moduleDialogCbDisableRingtoneOutCall.isChecked
                ValDisInCall.booleanVal = binding.moduleDialogCbDisableRingtoneInCall.isChecked
            }
            negativeButton()
        }
    }

    override fun initOnce() {
        MethodPlaySound.toDexMethod {
            hook {
                beforeIfEnabled {
                    val params = args(1).cast<Bundle>()!!
                    val scene = params.getString("scene")
                    when (scene) {
                        "start" -> {
                            val isOutCall = params.getBoolean("isOutCall")
                            val disOutCall = (isOutCall && ValDisOutCall.booleanVal)
                            val disInCall = (!isOutCall && ValDisInCall.booleanVal)
                            if (disOutCall || disInCall) {
                                resultFalse()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodPlaySound.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.BaseSceneSetting", "playSound Failed Throwable t = ")
                }
            }
        }
    }
}
