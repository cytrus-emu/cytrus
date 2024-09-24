// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.model.view

class RunnableSetting(
    titleId: Int,
    descriptionId: Int,
    val isRuntimeRunnable: Boolean,
    val runnable: () -> Unit,
    val value: (() -> String)? = null
) : SettingsItem(null, titleId, descriptionId) {
    override val type = TYPE_RUNNABLE
}
