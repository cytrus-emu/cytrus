// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cytrus.cytrus_emu.CytrusApplication
import org.cytrus.cytrus_emu.R
import org.cytrus.cytrus_emu.databinding.DialogCopyDirBinding
import org.cytrus.cytrus_emu.model.SetupCallback
import org.cytrus.cytrus_emu.utils.CytrusDirectoryHelper
import org.cytrus.cytrus_emu.utils.FileUtil
import org.cytrus.cytrus_emu.utils.PermissionsHandler
import org.cytrus.cytrus_emu.viewmodel.HomeViewModel

class CopyDirProgressDialog : DialogFragment() {
    private var _binding: DialogCopyDirBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCopyDirBinding.inflate(layoutInflater)

        isCancelable = false
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.moving_data)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.apply {
            launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    homeViewModel.messageText.collectLatest { binding.messageText.text = it }
                }
            }
            launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    homeViewModel.dirProgress.collectLatest {
                        binding.progressBar.max = homeViewModel.maxDirProgress.value
                        binding.progressBar.progress = it
                    }
                }
            }
            launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    homeViewModel.copyComplete.collect {
                        if (it) {
                            homeViewModel.setUserDir(
                                requireActivity(),
                                PermissionsHandler.cytrusDirectory.path!!
                            )
                            homeViewModel.copyInProgress = false
                            homeViewModel.setPickingUserDir(false)
                            Toast.makeText(
                                requireContext(),
                                R.string.copy_complete,
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "CopyDirProgressDialog"

        fun newInstance(
            activity: FragmentActivity,
            previous: Uri,
            path: Uri,
            callback: SetupCallback? = null
        ): CopyDirProgressDialog? {
            val viewModel = ViewModelProvider(activity)[HomeViewModel::class.java]
            if (viewModel.copyInProgress) {
                return null
            }
            viewModel.clearCopyInfo()
            viewModel.copyInProgress = true

            activity.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    FileUtil.copyDir(
                        previous.toString(),
                        path.toString(),
                        object : FileUtil.CopyDirListener {
                            override fun onSearchProgress(directoryName: String) {
                                viewModel.onUpdateSearchProgress(
                                    CytrusApplication.appContext.resources,
                                    directoryName
                                )
                            }

                            override fun onCopyProgress(filename: String, progress: Int, max: Int) {
                                viewModel.onUpdateCopyProgress(
                                    CytrusApplication.appContext.resources,
                                    filename,
                                    progress,
                                    max
                                )
                            }

                            override fun onComplete() {
                                CytrusDirectoryHelper.initializeCytrusDirectory(path)
                                callback?.onStepCompleted()
                                viewModel.setCopyComplete(true)
                            }
                        })
                }
            }
            return CopyDirProgressDialog()
        }
    }
}
