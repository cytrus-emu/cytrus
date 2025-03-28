// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.model

enum class BooleanSetting(
    override val key: String,
    override val section: String,
    override val defaultValue: Boolean
) : AbstractBooleanSetting {
    SPIRV_SHADER_GEN("spirv_shader_gen", Settings.SECTION_RENDERER, true),
    ASYNC_SHADERS("async_shader_compilation", Settings.SECTION_RENDERER, false),
    PLUGIN_LOADER("plugin_loader", Settings.SECTION_SYSTEM, false),
    ALLOW_PLUGIN_LOADER("allow_plugin_loader", Settings.SECTION_SYSTEM, true),
    SWAP_SCREEN("swap_screen", Settings.SECTION_LAYOUT, false),
    PLAY_HAPTICS("play_haptics", Settings.SECTION_CONTROLS, true);

    override var boolean: Boolean = defaultValue

    override val valueAsString: String
        get() = boolean.toString()

    override val isRuntimeEditable: Boolean
        get() {
            for (setting in NOT_RUNTIME_EDITABLE) {
                if (setting == this) {
                    return false
                }
            }
            return true
        }

    companion object {
        private val NOT_RUNTIME_EDITABLE = listOf(
            PLUGIN_LOADER,
            ALLOW_PLUGIN_LOADER
        )

        fun from(key: String): BooleanSetting? =
            BooleanSetting.entries.firstOrNull { it.key == key }

        fun clear() = BooleanSetting.entries.forEach { it.boolean = it.defaultValue }
    }
}
