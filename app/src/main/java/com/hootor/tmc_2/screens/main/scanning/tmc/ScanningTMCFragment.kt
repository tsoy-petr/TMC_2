package com.hootor.tmc_2.screens.main.scanning.tmc

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.databinding.FragmentScanningBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.screens.main.photo.PhotoFragment.Companion.KEY_ARGS_SAVE_URI
import com.hootor.tmc_2.screens.main.photo.UploadPhotoFragment
import com.hootor.tmc_2.screens.main.photo.UploadPhotoFragment.Companion.KEY_ARGS_URI_IMAGE
import com.hootor.tmc_2.screens.main.scanning.qr.ScanningViewModel
import com.hootor.tmc_2.screens.main.scanning.tmc.adapter.*
import com.hootor.tmc_2.screens.main.scanning.tmc.adapter.decorations.FeedHorizontalDividerItemDecoration
import com.hootor.tmc_2.screens.main.scanning.tmc.adapter.decorations.GroupVerticalItemDecoration
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.FieldTMCDescription
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.HorizontalImgTMC
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.HorizontalItem
import com.hootor.tmc_2.screens.main.scanning.tmc.holders.TMCItem
import com.hootor.tmc_2.screens.main.tmcTree.TMCTreeFragment.Companion.KEY_ARGS_QR_CODE
import com.hootor.tmc_2.screens.view.bottomEndParentConstraint
import com.hootor.tmc_2.screens.view.getMarginBottom
import com.hootor.tmc_2.screens.view.matchParentConstraint
import com.hootor.tmc_2.utils.Event
import com.hootor.tmc_2.utils.findTopNavController
import com.hootor.tmc_2.utils.listenResults
import com.hootor.tmc_2.utils.observeEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScanningTMCFragment : Fragment(R.layout.fragment_scanning) {

    private val inInterpolator by lazy { FastOutSlowInInterpolator() }
    private var isAnimatingOut = false

    private lateinit var binding: FragmentScanningBinding

    private val descriptionFieldAdapter =
        FieldsTMCAdapter(listOf(FieldTMCBoolean(), FieldTMCDescription(::onClickTMCItem)))
    private lateinit var imgFieldAdapter: FieldsTMCAdapter

    private val concatAdapter by lazy {
        ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(false)
                .build(),
            imgFieldAdapter,
            descriptionFieldAdapter
        )
    }

    @Inject
    lateinit var prefs: Prefs

    private lateinit var viewModel: ScanningTMCViewModel
    private lateinit var viewModelScanning: ScanningViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as App).component
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),    // contract for requesting 1 permission
        ::onGotCameraPermissionResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        imgFieldAdapter = FieldsTMCAdapter(listOf(HorizontalImgTMC(prefs)))

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentScanningBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, viewModelFactory)[ScanningTMCViewModel::class.java]
        viewModelScanning = ViewModelProvider(requireActivity().viewModelStore,
            viewModelFactory)[ScanningViewModel::class.java]

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fState.collect {
                    handleEvent(it)
                }
            }
        }

        viewModelScanning.qrCode.observeEvent(viewLifecycleOwner) { qrCode ->
            viewModel.fetchData(qrCode)
        }

        initNewAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu()

        binding.floatingActionButtonToStartScanning.setOnClickListener {
            requestCameraPermissionLauncher.launch(mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray())
        }
        binding.buttonReload.setOnClickListener {
            viewModel.reload()
        }

        listenFragmentResult()

    }

    private fun listenFragmentResult() {
        listenResults<String>(KEY_ARGS_QR_CODE) {
            viewModel.fetchData(it)
        }

        listenResults<Uri?>(KEY_ARGS_SAVE_URI) {
            it?.let { uri: Uri ->
                findTopNavController().navigate(R.id.action_tabsFragment_to_upload_photo_graph,
                    bundleOf(
                        KEY_ARGS_URI_IMAGE to uri,
                        UploadPhotoFragment.KEY_ARGS_QR_CODE to viewModel.getCurrQrCode()
                    ))
            }
        }
    }

    private fun initMenu() {
        binding.topToolbar.inflateMenu(R.menu.tmc_scanning_menu)
        binding.topToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemMenuGetComposition -> {
                    viewModel.getCurrQrCode().takeIf {
                        it.isNotEmpty()
                    }?.let { qrCode ->
                        findTopNavController().navigate(R.id.action_tabsFragment_to_tmc_tree_graph,
                            bundleOf(KEY_ARGS_QR_CODE to qrCode))
                    }
                    true
                }
                R.id.itemMenuTakePhoto -> {
                    findTopNavController().navigate(R.id.action_tabsFragment_to_take_photo_graph)
                    true
                }
                else -> false
            }
        }
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    private fun initNewAdapter() {
        with(binding.itemsFieldsList) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter

            addItemDecoration(GroupVerticalItemDecoration(R.layout.item_tmc_field, 75, 45))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && binding.floatingActionButtonToStartScanning.visibility == View.VISIBLE) {
                        animateOut(binding.floatingActionButtonToStartScanning)
                    } else if (dy < 0 && binding.floatingActionButtonToStartScanning.visibility != View.VISIBLE) {
                        animateIn(binding.floatingActionButtonToStartScanning)
                    }
                }
            })
        }
    }

    private fun handleEvent(event: ViewState) {
        when (event.state) {
            is State.Loading -> {
                showLoading()
                showMenuItemComposition(false)
            }
            is State.Empty -> {
                showEmpty()
                showMenuItemComposition(false)
            }
            is State.Success -> {
                showSuccess(event)
                showMenuItemComposition(true)
            }
            is State.Error -> {
                showError(event.state)
                showMenuItemComposition(false)
            }
            is State.Init -> {
                showInit()
                showMenuItemComposition(false)
            }
        }
    }

    private fun showMenuItemComposition(isShow: Boolean) {
        binding.topToolbar.menu.apply {
            findItem(R.id.itemMenuGetComposition)?.let {
                it.isVisible = isShow

            }
            findItem(R.id.itemMenuTakePhoto)?.let {
                it.isVisible = isShow
            }
        }
    }

    private fun showInit() {
        binding.progressBar.isVisible = false
        binding.groupDataList.isVisible = false
        binding.floatingActionButtonToStartScanning.matchParentConstraint()
        setupError(false)
    }

    private fun showError(state: State.Error) {
        binding.floatingActionButtonToStartScanning.bottomEndParentConstraint()
        binding.groupDataList.isVisible = false
        binding.floatingActionButtonToStartScanning.isVisible = true
        binding.progressBar.isVisible = false
        setupError(true, state.message)
    }

    private fun showSuccess(event: ViewState) {

        imgFieldAdapter.submitList(
            event.items.filterIsInstance<HorizontalItem>().toList()
        )

        descriptionFieldAdapter.submitList(
            event.items.filter { it is TMCItemBoolean || it is TMCItem }
        )

        binding.groupDataList.isVisible = true

        binding.floatingActionButtonToStartScanning.isVisible = true
        binding.floatingActionButtonToStartScanning.bottomEndParentConstraint()
        binding.progressBar.isVisible = false

        setupError(false)
    }

    private fun showEmpty() {
        binding.groupDataList.isVisible = false

        binding.floatingActionButtonToStartScanning.visibility = View.VISIBLE
        binding.floatingActionButtonToStartScanning.bottomEndParentConstraint()

        binding.progressBar.isVisible = false

        setupError(false)
    }

    private fun showLoading() {
        binding.groupDataList.isVisible = false
        binding.floatingActionButtonToStartScanning.isVisible = false

        binding.progressBar.isVisible = true

        binding.floatingActionButtonToStartScanning.bottomEndParentConstraint()

        setupError(false)
    }

    private fun setupError(isShow: Boolean, textError: String? = null) {
        binding.groupError.isVisible = isShow
        binding.textViewError.text = textError
    }

    private fun onClickTMCItem(item: TMCItem) {}
//    private fun onClickTMCItem(item: TMCItem) =
//        viewModel.getCurrQrCode().takeIf {
//            it.isNotEmpty()
//        }?.let { qrCode ->
//            findTopNavController().navigate(R.id.action_tabsFragment_to_tmc_tree_graph,
//                bundleOf(KEY_ARGS_QR_CODE to qrCode))
//        }

    private fun animateOut(button: FloatingActionButton) {
        ViewCompat.animate(button)
            .translationY((button.height + button.getMarginBottom()).toFloat())
            .setInterpolator(inInterpolator).withLayer()
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {
                    isAnimatingOut = true
                }

                override fun onAnimationCancel(view: View) {
                    isAnimatingOut = false
                }

                override fun onAnimationEnd(view: View) {
                    isAnimatingOut = false
                    view.visibility = View.INVISIBLE
                }
            }).start()
    }

    private fun animateIn(button: FloatingActionButton) {
        button.visibility = View.VISIBLE
        ViewCompat.animate(button).translationY(0f)
            .setInterpolator(inInterpolator).withLayer()
            .setListener(null)
            .start()
    }

    private fun onGotCameraPermissionResult(isGranted: Map<String, Boolean>) {
        var granted = true
        isGranted.forEach {
            if (granted) {
                granted = it.value
            }
        }
        if (granted) {
            launchScanning()
        } else {
            // example of handling 'Deny & don't ask again' user choice
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                askUserForOpeningAppSettings()
            } else {
                Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun onGotPhotoPermissionResult(isGranted: Map<String, Boolean>) {
        var granted = true
        isGranted.forEach {
            if (granted) {
                granted = it.value
            }
        }
        if (granted) {

        } else {
            // example of handling 'Deny & don't ask again' user choice
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                askUserForOpeningAppSettings()
            } else {
                Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun askUserForOpeningAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
        )
        if (requireActivity().packageManager.resolveActivity(appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY) == null
        ) {
            Toast.makeText(requireContext(),
                R.string.permissions_denied_forever,
                Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(R.string.open) { _, _ ->
                    startActivity(appSettingsIntent)
                }
                .create()
                .show()
        }
    }

    private fun launchScanning() {
        findNavController().navigate(R.id.action_scanningTMCFragment_to_scanningQRFragment)
    }
}