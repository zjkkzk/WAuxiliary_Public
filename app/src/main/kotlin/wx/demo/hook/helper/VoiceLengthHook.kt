package wx.demo.hook.helper

import android.view.LayoutInflater
import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.databinding.ModuleDialogVoiceLengthBinding
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
object VoiceLengthHook : SwitchHook("VoiceLengthHook"), IDexFind {
    private object MethodSetVoice : DexDescData("VoiceLengthHook.MethodSetVoice")
    private object ValVoiceLength : DefaultData("VoiceLengthHook.ValVoiceLength", intDefVal = 1)

    override val location = "辅助"
    override val funcName = "语音时长"
    override val funcDesc = "可自定义修改发送的语音消息显示时长"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogVoiceLengthBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogSliderVoiceLength.value = ValVoiceLength.intVal.toFloat()
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValVoiceLength.intVal = binding.moduleDialogSliderVoiceLength.value.toInt()
            }
            negativeButton()
        }
    }

    override fun initOnce() {
        MethodSetVoice.toDexMethod {
            hook {
                beforeIfEnabled {
                    val objIndex = when {
                        args.size == 1 -> 0
                        args.size == 2 && args[0] is String -> 1
                        else -> return@beforeIfEnabled
                    }
                    val obj = args(objIndex).any()!!
                    obj.asResolver().firstField {
                        name = "l"
                        type = Int::class
                    }.set(ValVoiceLength.intVal * 1000)
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodSetVoice.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.VoiceStorage", "update failed, no values set")
                }
            }
        }
    }
}
