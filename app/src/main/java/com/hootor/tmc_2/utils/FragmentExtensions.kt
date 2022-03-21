package com.hootor.tmc_2.utils

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.hootor.tmc_2.R

fun Fragment.findTopNavController(): NavController {
    val topLevelHost = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment?
    return topLevelHost?.navController ?: findNavController()
}

typealias ResultsListener<T> = (T) -> Unit

/**
 * Send some results to the previous fragment.
 */
fun <T> Fragment.publishResults(key: String, result: T) {
    findTopNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

/**
 * Listen for screen results. Results are automatically cleared when the listener receives them.
 */
fun <T> Fragment.listenResults(key: String, listener: ResultsListener<T>) {
    val liveData = findTopNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)
    liveData?.observe(viewLifecycleOwner) { result ->
        if (result != null) {
            listener(result)
            liveData.value = null
        }
    }
}