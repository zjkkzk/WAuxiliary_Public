package wx.demo.hook.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Parcelable
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DexDescData
import me.hd.wauxv.data.factory.WxProcess
import me.hd.wauxv.databinding.ModuleDialogLocationBinding
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.dex.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.helper.utils.ActivityHelper
import me.hd.wauxv.hook.factory.toDexMethod
import me.hd.wauxv.hook.factory.toLazyAppClass
import me.hd.wauxv.ui.setting.factory.showConfigDialog
import org.luckypray.dexkit.DexKitBridge

@HookAnno
@ViewAnno
object LocationHook : SwitchHook("LocationHook"), IDexFind {
    private object MethodListener : DexDescData("LocationHook.MethodListener")
    private object MethodListenerWgs84 : DexDescData("LocationHook.MethodListenerWgs84")
    private object MethodDefaultManager : DexDescData("LocationHook.MethodDefaultManager")
    private object MethodSelectPoiMapOnClick : DexDescData("LocationHook.MethodSelectPoiMapOnClick")
    private object ValLatitude : DefaultData("LocationHook.ValLatitude", floatDefVal = LATITUDE_DEF_VAL)
    private object ValLongitude : DefaultData("LocationHook.ValLongitude", floatDefVal = LONGITUDE_DEF_VAL)

    private const val LATITUDE_DEF_VAL = 31.135633f
    private const val LONGITUDE_DEF_VAL = 121.66625f
    private lateinit var binding: ModuleDialogLocationBinding
    private val RedirectUIClass by "com.tencent.mm.plugin.location.ui.RedirectUI".toLazyAppClass()

    override val location = "辅助"
    override val funcName = "虚拟定位"
    override val funcDesc = "将腾讯定位SDK结果虚拟为指定经纬度"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        binding = ModuleDialogLocationBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogBtnLocationSelect.setOnClickListener {
            val activity = ActivityHelper.getTopActivity()!!
            activity.startActivityForResult(Intent(layoutView.context, RedirectUIClass).apply { putExtra("map_view_type", 8) }, 6)
        }
        binding.moduleDialogEdtLocationLatitude.setText("${ValLatitude.floatVal}")
        binding.moduleDialogEdtLocationLongitude.setText("${ValLongitude.floatVal}")
        layoutView.context.showConfigDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValLatitude.floatVal = binding.moduleDialogEdtLocationLatitude.text.toString().toFloat()
                ValLongitude.floatVal = binding.moduleDialogEdtLocationLongitude.text.toString().toFloat()
            }
            neutralButton("重置") {
                ValLatitude.floatVal = LATITUDE_DEF_VAL
                ValLongitude.floatVal = LONGITUDE_DEF_VAL
            }
            negativeButton()
        }
    }
    override val targetProcess = arrayOf(
        WxProcess.MAIN_PROCESS.processName,
        WxProcess.APP_BRAND_0.processName
    )
    override val isNeedRestartApp = true

    @Suppress("DEPRECATION")
    override fun initOnce() {
        RedirectUIClass.resolve().firstMethod {
            name = "onActivityResult"
            parameters(Int::class, Int::class, Intent::class)
        }.hook {
            afterIfEnabled {
                val requestCode = args(0).cast<Int>()!!
                val resultCode = args(1).cast<Int>()!!
                if (requestCode == 6 && resultCode == Activity.RESULT_OK) {
                    val intent = args(2).cast<Intent>()!!
                    val locationIntent = intent.getParcelableExtra<Parcelable>("KLocationIntent")!!
                    val locationDataStr = locationIntent.asResolver().firstMethod { returnType = String::class }.invoke<String>()!!
                    val pattern = Regex("lat ([-+]?[0-9]*\\.?[0-9]+);lng ([-+]?[0-9]*\\.?[0-9]+);")
                    val match = pattern.find(locationDataStr)
                    if (match != null && match.groupValues.size == 3) {
                        binding.moduleDialogEdtLocationLatitude.setText("${match.groupValues[1].toFloatOrNull() ?: LATITUDE_DEF_VAL}")
                        binding.moduleDialogEdtLocationLongitude.setText("${match.groupValues[2].toFloatOrNull() ?: LONGITUDE_DEF_VAL}")
                    } else {
                        binding.moduleDialogEdtLocationLatitude.setText("$LATITUDE_DEF_VAL")
                        binding.moduleDialogEdtLocationLongitude.setText("$LONGITUDE_DEF_VAL")
                    }
                }
            }
        }
        listOf(MethodListener, MethodListenerWgs84, MethodDefaultManager).forEach { descData ->
            descData.toDexMethod {
                hook {
                    beforeIfEnabled {
                        args(0).any()?.also { location ->
                            location.asResolver().apply {
                                firstMethod { name = "getLatitude" }.hook {
                                    beforeIfEnabled {
                                        result = ValLatitude.floatVal.toDouble()
                                    }
                                }
                                firstMethod { name = "getLongitude" }.hook {
                                    beforeIfEnabled {
                                        result = ValLongitude.floatVal.toDouble()
                                    }
                                }
                            }
                        }
                        removeSelf()
                    }
                }
            }
        }
        MethodSelectPoiMapOnClick.toDexMethod {
            hook {
                beforeIfEnabled {
                    val view = args(0).cast<View>()!!
                    AlertDialog.Builder(view.context).apply {
                        setTitle("修改经纬度")
                        setView(LinearLayout(context).apply {
                            gravity = Gravity.CENTER
                            orientation = RadioGroup.HORIZONTAL
                            addView(EditText(context).apply {
                                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                                setText("${ValLatitude.floatVal}")
                                addTextChangedListener {
                                    doAfterTextChanged {
                                        val input = it.toString().toFloatOrNull()
                                        if (input != null) {
                                            ValLatitude.floatVal = input
                                        }
                                    }
                                }
                            })
                            addView(EditText(context).apply {
                                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                                setText("${ValLongitude.floatVal}")
                                addTextChangedListener {
                                    doAfterTextChanged {
                                        val input = it.toString().toFloatOrNull()
                                        if (input != null) {
                                            ValLongitude.floatVal = input
                                        }
                                    }
                                }
                            })
                        })
                        setPositiveButton("确定", null)
                        setNegativeButton("取消", null)
                    }.show()
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodListener.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    name = "onLocationChanged"
                    usingEqStrings("MicroMsg.SLocationListener")
                }
            }
        }
        MethodListenerWgs84.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    name = "onLocationChanged"
                    usingEqStrings("MicroMsg.SLocationListenerWgs84")
                }
            }
        }
        MethodDefaultManager.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    name = "onLocationChanged"
                    usingEqStrings("MicroMsg.DefaultTencentLocationManager", "[mlocationListener]error:%d, reason:%s")
                }
            }
        }
        MethodSelectPoiMapOnClick.findDexClassMethod(dexKit) {
            onMethod {
                searchPackages("com.tencent.mm.plugin.location.ui.impl")
                matcher {
                    usingEqStrings("MicroMsg.MMPoiMapUI", "invalid lat lng")
                }
            }
        }
    }
}
