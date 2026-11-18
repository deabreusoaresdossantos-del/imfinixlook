package com.imfinix.look

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ImfinixAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        var instance: ImfinixAccessibilityService? = null

        /** Ações globais do sistema via acessibilidade. Retorna true se executou. */
        fun perform(globalAction: Int): Boolean {
            val svc = instance ?: return false
            return try {
                svc.performGlobalAction(globalAction)
            } catch (e: Exception) {
                false
            }
        }
    }
}
