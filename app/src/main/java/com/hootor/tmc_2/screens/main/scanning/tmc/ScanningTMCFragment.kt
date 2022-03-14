package com.hootor.tmc_2.screens.main.scanning.tmc

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hootor.tmc_2.App
import com.hootor.tmc_2.R
import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.databinding.FragmentScanningBinding
import com.hootor.tmc_2.di.ViewModelFactory
import com.hootor.tmc_2.screens.main.scanning.qr.ScanningViewModel
import com.hootor.tmc_2.screens.view.bottomEndParentConstraint
import com.hootor.tmc_2.screens.view.matchParentConstraint
import com.hootor.tmc_2.utils.observeEvent
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScanningTMCFragment : Fragment(R.layout.fragment_scanning) {

    private val inInterpolator by lazy { FastOutSlowInInterpolator() }
    private var isAnimatingOut = false

    private lateinit var binding: FragmentScanningBinding
    private lateinit var adapter: TMCFieldsAdapter
    private lateinit var adapterImg: SliderAdapterTMC

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
        ActivityResultContracts.RequestPermission(),    // contract for requesting 1 permission
        ::onGotCameraPermissionResult
    )

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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.fState.collect {
                    handleEvent(it)
                }
            }
        }

        viewModelScanning.qrCode.observeEvent(viewLifecycleOwner) { qrCode ->
            viewModel.fetchData(qrCode)
        }

        initImgAdapter()
        initAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButtonToStartScanning.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    private fun initImgAdapter() {
        adapterImg = SliderAdapterTMC(prefs)
        binding.imageSlider.setSliderAdapter(adapterImg)

        binding.imageSlider.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        binding.imageSlider.indicatorSelectedColor = Color.WHITE
        binding.imageSlider.indicatorUnselectedColor = Color.GRAY
        binding.imageSlider.scrollTimeInSec = 30

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.currImgListPosition.collect { position ->
                    binding.imageSlider.currentPagePosition = position
                }
            }
        }

        binding.imageSlider.setOnIndicatorClickListener { position ->
            binding.imageSlider.currentPagePosition = position
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.setCurrImgListPosition(binding.imageSlider.currentPagePosition)
        super.onSaveInstanceState(outState)
    }

    private fun initAdapter() {
        adapter = TMCFieldsAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        binding.itemsFieldsList.layoutManager = layoutManager
        binding.itemsFieldsList.adapter = adapter

        binding.itemsFieldsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun handleEvent(event: ViewState) {
        when (event.state) {
            is State.Loading -> {
                showLoading()
            }
            is State.Empty -> {
                showEmpty()
            }
            is State.Success -> {
                showSuccess(event)
            }
            is State.Error -> {
                showError(event.state)
            }
            is State.Init -> {
                showInit()
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
        adapterImg.items = event.imgs
        adapter.items = event.items

        binding.groupDataList.isVisible = true

        binding.floatingActionButtonToStartScanning.isVisible = true
        binding.floatingActionButtonToStartScanning.bottomEndParentConstraint()
        binding.progressBar.isVisible = false

        binding.imageSlider.isVisible = event.imgs.isNotEmpty()

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

    private fun animateOut(button: FloatingActionButton) {
        ViewCompat.animate(button).translationY((button.height + getMarginBottom(button)).toFloat())
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

    private fun getMarginBottom(v: View): Int {
        var marginBottom = 0
        val layoutParams = v.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) marginBottom = layoutParams.bottomMargin
        return marginBottom
    }

    private fun onGotCameraPermissionResult(granted: Boolean) {
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