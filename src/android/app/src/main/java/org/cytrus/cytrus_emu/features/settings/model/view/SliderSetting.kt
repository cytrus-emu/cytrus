// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.model.view

import org.cytrus.cytrus_emu.features.settings.model.AbstractFloatSetting
import org.cytrus.cytrus_emu.features.settings.model.AbstractIntSetting
import org.cytrus.cytrus_emu.features.settings.model.AbstractSetting
import org.cytrus.cytrus_emu.features.settings.model.FloatSetting
import org.cytrus.cytrus_emu.features.settings.model.ScaledFloatSetting
import org.cytrus.cytrus_emu.utils.Log
import kotlin.math.roundToInt

class SliderSetting(
    setting: AbstractSetting?,
    titleId: Int,
    descriptionId: Int,
    val min: Int,
    val max: Int,
    val units: String,
    val key: String? = null,
    val defaultValue: Float? = null
) : SettingsItem(setting, titleId, descriptionId) {
    override val type = TYPE_SLIDER

    val selectedValue: Int
        get() {
            val setting = setting ?: return defaultValue!!.toInt()
            return when (setting) {
                is AbstractIntSetting -> setting.int
                is FloatSetting -> setting.float.roundToInt()
                is ScaledFloatSetting -> setting.float.roundToInt()
                else -> {
                    Log.error("[SliderSetting] Error casting setting type.")
                    -1
                }
            }
        }

    /**
     * Write a value to the backing int. If that int was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the int.
     * @return the existing setting with the new value applied.
     */
    fun setSelectedValue(selection: Int): AbstractIntSetting {
        val intSetting = setting as AbstractIntSetting
        intSetting.int = selection
        return intSetting
    }

    /**
     * Write a value to the backing float. If that float was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the float.
     * @return the existing setting with the new value applied.
     */
    fun setSelectedValue(selection: Float): AbstractFloatSetting {
        val floatSetting = setting as AbstractFloatSetting
        if (floatSetting is ScaledFloatSetting) {
            floatSetting.float = selection
        } else {
            floatSetting.float = selection
        }
        return floatSetting
    }
}
