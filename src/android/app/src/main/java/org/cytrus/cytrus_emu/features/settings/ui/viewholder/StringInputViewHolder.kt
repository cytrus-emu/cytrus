// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.ui.viewholder

import android.view.View
import org.cytrus.cytrus_emu.databinding.ListItemSettingBinding
import org.cytrus.cytrus_emu.features.settings.model.view.SettingsItem
import org.cytrus.cytrus_emu.features.settings.model.view.StringInputSetting
import org.cytrus.cytrus_emu.features.settings.ui.SettingsAdapter

class StringInputViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: SettingsItem

    override fun bind(item: SettingsItem) {
        setting = item
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.visibility = View.VISIBLE
            binding.textSettingDescription.setText(item.descriptionId)
        } else {
            binding.textSettingDescription.visibility = View.GONE
        }
        binding.textSettingValue.visibility = View.VISIBLE
        binding.textSettingValue.text = setting.setting?.valueAsString
    }

    override fun onClick(clicked: View) {
        if (!setting.isEditable) {
            adapter.onClickDisabledSetting()
            return
        }
        adapter.onStringInputClick((setting as StringInputSetting), bindingAdapterPosition)
    }

    override fun onLongClick(clicked: View): Boolean {
        if (setting.isEditable) {
            return adapter.onLongClick(setting.setting!!, bindingAdapterPosition)
        } else {
            adapter.onClickDisabledSetting()
        }
        return false
    }
}
