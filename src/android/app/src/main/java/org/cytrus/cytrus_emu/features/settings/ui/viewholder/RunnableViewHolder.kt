// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.features.settings.ui.viewholder

import android.view.View
import org.cytrus.cytrus_emu.NativeLibrary
import org.cytrus.cytrus_emu.databinding.ListItemSettingBinding
import org.cytrus.cytrus_emu.features.settings.model.view.RunnableSetting
import org.cytrus.cytrus_emu.features.settings.model.view.SettingsItem
import org.cytrus.cytrus_emu.features.settings.ui.SettingsAdapter

class RunnableViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: RunnableSetting

    override fun bind(item: SettingsItem) {
        setting = item as RunnableSetting
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.setText(item.descriptionId)
            binding.textSettingDescription.visibility = View.VISIBLE
        } else {
            binding.textSettingDescription.visibility = View.GONE
        }

        if (setting.value != null) {
            binding.textSettingValue.visibility = View.VISIBLE
            binding.textSettingValue.text = setting.value!!.invoke()
        } else {
            binding.textSettingValue.visibility = View.GONE
        }

        if (setting.isEditable) {
            binding.textSettingName.alpha = 1f
            binding.textSettingDescription.alpha = 1f
            binding.textSettingValue.alpha = 1f
        } else {
            binding.textSettingName.alpha = 0.5f
            binding.textSettingDescription.alpha = 0.5f
            binding.textSettingValue.alpha = 0.5f
        }
    }

    override fun onClick(clicked: View) {
        if (!setting.isRuntimeRunnable && !NativeLibrary.isRunning()) {
            setting.runnable.invoke()
        } else {
            adapter.onClickDisabledSetting()
        }
    }

    override fun onLongClick(clicked: View): Boolean {
        // no-op
        return true
    }
}
