package com.example.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

class CallTool(private val context: Context) : JarvisTool {
    override val name = "make_call"
    override val description = "Prepares or places a phone call to a given phone number or contact identifier."
    override val parameters = listOf(
        ToolParameter(
            name = "phone_number",
            type = "string",
            description = "The phone number or contact name to call (e.g. '911', '+1234567890', 'Mom').",
            required = true
        ),
        ToolParameter(
            name = "direct_call",
            type = "boolean",
            description = "True to place call immediately if CALL_PHONE permission is granted, false to open the dialer for safety.",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val target = arguments["phone_number"]?.toString()?.trim()
            ?: return ToolResult(false, "Phone number or target parameter is required.")

        val directCall = arguments["direct_call"] as? Boolean ?: false
        val cleanNumber = target.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return if (directCall && hasCallPermission && cleanNumber.isNotEmpty()) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                ToolResult(true, "Direct call placed to $target.", mapOf("target" to target, "action" to "CALL"))
            } catch (e: Exception) {
                ToolResult(false, "Failed to place call: ${e.message}")
            }
        } else {
            try {
                // Safe dialer fallback
                val dialUri = if (cleanNumber.isNotEmpty()) Uri.parse("tel:$cleanNumber") else Uri.parse("tel:")
                val dialIntent = Intent(Intent.ACTION_DIAL, dialUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)

                val note = if (!hasCallPermission && directCall) {
                    " (Note: Direct CALL_PHONE permission is not granted; dialer launched for confirmation)"
                } else ""

                ToolResult(
                    true,
                    "Opening system phone dialer for $target$note.",
                    mapOf("target" to target, "action" to "DIAL", "permissionGranted" to hasCallPermission)
                )
            } catch (e: Exception) {
                ToolResult(false, "Failed to open phone dialer: ${e.message}")
            }
        }
    }
}
