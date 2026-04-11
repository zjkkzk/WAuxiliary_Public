package wx.demo.hook.helper

import android.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.classOf
import com.highcapable.yukihookapi.hook.param.HookParam
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.databinding.ModuleDialogEmojiGameBinding
import me.hd.wauxv.ui.setting.factory.showConfigDialog
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.helper.utils.ActivityHelper
import me.hd.wauxv.hook.factory.toDexMethod
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.MatchType
import kotlin.random.Random

@HookAnno
@ViewAnno
object EmojiGameHook : SwitchHook("EmojiGameHook"), IDexFind {
    enum class MorraType(val index: Int, val chineseName: String) {
        SCISSORS(0, "剪刀"),
        STONE(1, "石头"),
        PAPER(2, "布")
    }

    enum class DiceFace(val index: Int, val chineseName: String) {
        ONE(0, "一"),
        TWO(1, "二"),
        THREE(2, "三"),
        FOUR(3, "四"),
        FIVE(4, "五"),
        SIX(5, "六"),
    }

    private object MethodRandom : DexDescData("EmojiGameHook.MethodRandom")
    private object MethodPanelClick : DexDescData("EmojiGameHook.MethodPanelClick")
    private object ValMorra : DefaultData("EmojiGameHook.ValMorra", intDefVal = 0)
    private object ValDice : DefaultData("EmojiGameHook.ValDice", intDefVal = 0)

    private const val MD5_MORRA = "9bd1281af3a31710a45b84d736363691"
    private const val MD5_DICE = "08f223fa83f1ca34e143d1e580252c7c"

    override val location = "辅助"
    override val funcName = "表情游戏"
    override val funcDesc = "预先自定义设置猜拳和骰子的随机结果"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogEmojiGameBinding.inflate(LayoutInflater.from(layoutView.context))
        when (ValMorra.intVal) {
            MorraType.SCISSORS.index -> binding.moduleDialogRbEmojiGameMorra0.isChecked = true
            MorraType.STONE.index -> binding.moduleDialogRbEmojiGameMorra1.isChecked = true
            MorraType.PAPER.index -> binding.moduleDialogRbEmojiGameMorra2.isChecked = true
        }
        when (ValDice.intVal) {
            DiceFace.ONE.index -> binding.moduleDialogRbEmojiGameDice1.isChecked = true
            DiceFace.TWO.index -> binding.moduleDialogRbEmojiGameDice2.isChecked = true
            DiceFace.THREE.index -> binding.moduleDialogRbEmojiGameDice3.isChecked = true
            DiceFace.FOUR.index -> binding.moduleDialogRbEmojiGameDice4.isChecked = true
            DiceFace.FIVE.index -> binding.moduleDialogRbEmojiGameDice5.isChecked = true
            DiceFace.SIX.index -> binding.moduleDialogRbEmojiGameDice6.isChecked = true
        }
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                when (binding.moduleDialogRgEmojiGameMorra.checkedRadioButtonId) {
                    binding.moduleDialogRbEmojiGameMorra0.id -> ValMorra.intVal = MorraType.SCISSORS.index
                    binding.moduleDialogRbEmojiGameMorra1.id -> ValMorra.intVal = MorraType.STONE.index
                    binding.moduleDialogRbEmojiGameMorra2.id -> ValMorra.intVal = MorraType.PAPER.index
                }
                when (binding.moduleDialogRgEmojiGameDice.checkedRadioButtonId) {
                    binding.moduleDialogRbEmojiGameDice1.id -> ValDice.intVal = DiceFace.ONE.index
                    binding.moduleDialogRbEmojiGameDice2.id -> ValDice.intVal = DiceFace.TWO.index
                    binding.moduleDialogRbEmojiGameDice3.id -> ValDice.intVal = DiceFace.THREE.index
                    binding.moduleDialogRbEmojiGameDice4.id -> ValDice.intVal = DiceFace.FOUR.index
                    binding.moduleDialogRbEmojiGameDice5.id -> ValDice.intVal = DiceFace.FIVE.index
                    binding.moduleDialogRbEmojiGameDice6.id -> ValDice.intVal = DiceFace.SIX.index
                }
            }
            negativeButton()
        }
    }

    private fun showSelectMorra(param: HookParam) {
        param.resultNull()
        val activity = ActivityHelper.getTopActivity()
        AlertDialog.Builder(activity).apply {
            setTitle("选择猜拳")
            setView(RadioGroup(context).apply {
                gravity = Gravity.CENTER
                orientation = RadioGroup.HORIZONTAL
                MorraType.entries.forEach { morraType ->
                    addView(RadioButton(context).apply {
                        id = morraType.index
                        text = morraType.chineseName
                        setOnClickListener {
                            ValMorra.intVal = morraType.index
                        }
                    })
                }
            })
            setPositiveButton("发送") { _, _ ->
                param.callOriginal()
            }
            setNeutralButton("随机") { _, _ ->
                ValMorra.intVal = Random.nextInt(0, 3)
                param.callOriginal()
            }
            setNegativeButton("取消", null)
        }.show()
    }

    private fun showSelectDice(param: HookParam) {
        param.resultNull()
        val activity = ActivityHelper.getTopActivity()
        AlertDialog.Builder(activity).apply {
            setTitle("选择骰子")
            setView(RadioGroup(context).apply {
                gravity = Gravity.CENTER
                orientation = RadioGroup.HORIZONTAL
                DiceFace.entries.forEach { diceFace ->
                    addView(RadioButton(context).apply {
                        id = diceFace.index
                        text = diceFace.chineseName
                        setOnClickListener {
                            ValDice.intVal = diceFace.index
                        }
                    })
                }
            })
            setPositiveButton("发送") { _, _ ->
                param.callOriginal()
            }
            setNeutralButton("随机") { _, _ ->
                ValDice.intVal = Random.nextInt(0, 6)
                param.callOriginal()
            }
            setNegativeButton("取消", null)
        }.show()
    }

    override fun initOnce() {
        MethodRandom.toDexMethod {
            hook {
                afterIfEnabled {
                    val type = args(0).int()
                    val originResult = result<Int>()
                    result = when (type) {
                        2 -> ValMorra.intVal
                        5 -> ValDice.intVal
                        else -> originResult
                    }
                }
            }
        }
        MethodPanelClick.toDexMethod {
            hook {
                beforeIfEnabled {
                    val obj = args(3).any()!!
                    val infoType = obj.asResolver().firstField { modifiers(Modifiers.FINAL);type = Int::class }.get<Int>()!!
                    if (infoType == 0) {
                        val emojiInfo = obj.asResolver().firstField { type = "com.tencent.mm.api.IEmojiInfo" }.get()!!
                        val emojiMd5 = emojiInfo.asResolver().firstMethod { name = "getMd5" }.invoke<String>()!!
                        when (emojiMd5) {
                            MD5_MORRA -> showSelectMorra(this)
                            MD5_DICE -> showSelectDice(this)
                        }
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodRandom.findDexClassMethod(dexKit) {
            onMethod {
                searchPackages("com.tencent.mm.sdk.platformtools")
                matcher {
                    returnType(classOf<Int>())
                    paramTypes(classOf<Int>(), classOf<Int>())
                    invokeMethods {
                        add { name = "currentTimeMillis" }
                        add { name = "nextInt" }
                        matchType = MatchType.Contains
                    }
                }
            }
        }
        MethodPanelClick.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.EmojiPanelClickListener", "penn send capture emoji click emoji: %s status: %d.")
                }
            }
        }
    }
}
