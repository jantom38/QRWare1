package com.example.shared.ui.navigation

/**
 * Simple multiplatform-friendly navigator abstraction used by common UI.
 */
interface Navigator {
    fun navigate(route: String)
    fun navigateUp()
    fun popBackStack(): Boolean
    fun navigateAndClearBackStack(route: String)
}
