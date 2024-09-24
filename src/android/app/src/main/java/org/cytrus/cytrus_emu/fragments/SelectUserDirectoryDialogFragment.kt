// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.cytrus.cytrus_emu.R
import org.cytrus.cytrus_emu.ui.main.MainActivity
import org.cytrus.cytrus_emu.viewmodel.HomeViewModel

class SelectUserDirectoryDialogFragment : DialogFragment() {
    private lateinit var mainActivity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mainActivity = requireActivity() as MainActivity

        isCancelable = false
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_cytrus_user_folder)
            .setMessage(R.string.cannot_skip_directory_description)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                mainActivity.openCytrusDirectory.launch(Uri.parse(null))
            }
            .show()
    }

    companion object {
        const val TAG = "SelectUserDirectoryDialogFragment"

        fun newInstance(activity: FragmentActivity): SelectUserDirectoryDialogFragment {
            ViewModelProvider(activity)[HomeViewModel::class.java].setPickingUserDir(true)
            return SelectUserDirectoryDialogFragment()
        }
    }
}
