// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.model.view

class SubmenuSetting(
    titleId: Int,
    descriptionId: Int,
    val menuKey: String
) : SettingsItem(null, titleId, descriptionId) {
    override val type = TYPE_SUBMENU
}
