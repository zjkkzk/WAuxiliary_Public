package wx.demo.hook.chat

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.databinding.ModuleDialogMsgFormatBinding
import me.hd.wauxv.ui.setting.factory.showConfigDialog
import me.hd.wauxv.factory.toDateStr
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexConstructor
import org.luckypray.dexkit.DexKitBridge

@HookAnno
@ViewAnno
object MsgFormatHook : SwitchHook("MsgFormatHook"), IDexFind {
    private object ConstructorSendTextComponent : DexDescData("MsgFormatHook.ConstructorSendTextComponent")
    private object ValTextFormat : DefaultData("MsgFormatHook.ValTextFormat", stringDefVal = TEXT_FORMAT_DEF_VAL)
    private object ValTimeFormat : DefaultData("MsgFormatHook.ValTimeFormat", stringDefVal = TIME_FORMAT_DEF_VAL)

    private const val TEXT_FORMAT_DEF_VAL = "\${sendText}喵~"
    private const val TIME_FORMAT_DEF_VAL = "HH:mm:ss"
    private val availablePlaceholders = listOf(
        "\${sendText}", "\${line}", "\${sendTime}"
    )

    override val location = "聊天"
    override val funcName = "发送文本格式"
    override val funcDesc = "将聊天发送的文本进行自定义格式处理"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogMsgFormatBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogEdtMsgFormatTextFormat.setText(ValTextFormat.stringVal)
        binding.moduleDialogEdtMsgFormatTimeFormat.setText(ValTimeFormat.stringVal)
        binding.moduleDialogEdtMsgFormatTextPlaceholders.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder("点击占位符自动添加以下字段:\n").apply {
                availablePlaceholders.forEach { placeholder ->
                    val startOffset = length
                    append("$placeholder ")
                    val endOffset = length - 1
                    setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                val selectionStart = binding.moduleDialogEdtMsgFormatTextFormat.selectionStart
                                val selectionEnd = binding.moduleDialogEdtMsgFormatTextFormat.selectionEnd
                                binding.moduleDialogEdtMsgFormatTextFormat.text?.replace(selectionStart, selectionEnd, placeholder)
                            }
                        }, startOffset, endOffset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValTextFormat.stringVal = binding.moduleDialogEdtMsgFormatTextFormat.text.toString()
                ValTimeFormat.stringVal = binding.moduleDialogEdtMsgFormatTimeFormat.text.toString()
            }
            neutralButton("重置") {
                ValTextFormat.stringVal = TEXT_FORMAT_DEF_VAL
                ValTimeFormat.stringVal = TIME_FORMAT_DEF_VAL
            }
            negativeButton()
        }
    }

    private fun formatMsg(msg: String): String {
        return ValTextFormat.stringVal
            .replace("\${sendText}", msg)
            .replace("\${line}", "\n")
            .replace("\${sendTime}", System.currentTimeMillis().toDateStr(ValTimeFormat.stringVal))
    }

    override fun initOnce() {
        ConstructorSendTextComponent.toDexConstructor {
            hook {
                beforeIfEnabled {
                    val textIndex = when (parameterCount) {
                        14 -> 8
                        13 -> 8
                        else -> 7
                    }
                    val originalText = args(textIndex).string()
                    args(textIndex).set(formatMsg(originalText))
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        ConstructorSendTextComponent.findDexClassMethod(dexKit) {
            onClass {
                searchPackages("com.tencent.mm.ui.chatting.component")
                matcher {
                    usingEqStrings("MicroMsg.ChattingUI.SendTextComponent", "doSendMessage begin send txt msg")
                }
            }
            onMethod {
                matcher {
                    paramCount(12..14)
                }
            }
        }
    }
}
