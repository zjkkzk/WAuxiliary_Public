package wx.demo.hook.highrisk

import android.view.LayoutInflater
import android.view.View
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.databinding.ModuleDialogSportStepBinding
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
object SportStepHook : SwitchHook("SportStepHook"), IDexFind {
    private object MethodGetTodayStep : DexDescData("SportStepHook.MethodGetTodayStep")
    private object ValSportModifyStep : DefaultData("SportStepHook.ValSportModifyStep", longDefVal = 88888)

    override val location = "高危"
    override val funcName = "运动步数"
    override val funcDesc = "启用后需要多次打开微信运动使其变化"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogSportStepBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogEdtSportModifyStep.setText(ValSportModifyStep.longVal.toString())
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValSportModifyStep.longVal = binding.moduleDialogEdtSportModifyStep.text.toString().toLongOrNull() ?: 88888
            }
            negativeButton()
        }
    }

    override fun initOnce() {
        MethodGetTodayStep.toDexMethod {
            hook {
                afterIfEnabled {
                    result = minOf(ValSportModifyStep.longVal, 98800L)
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodGetTodayStep.findDexClassMethod(dexKit) {
            onMethod {
                searchPackages("com.tencent.mm.plugin.sport.model")
                matcher {
                    usingEqStrings("MicroMsg.Sport.DeviceStepManager", "get today step from %s todayStep %d")
                }
            }
        }
    }
}
