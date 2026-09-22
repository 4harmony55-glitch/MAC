package com.mac.app.domain

object BuiltInCatalog {
    val triggers = listOf(
        NodeSpec(type=NodeType.TRIGGER, key="manual", title="Manual trigger", description="Run this macro from MAC."),
        NodeSpec(type=NodeType.TRIGGER, key="boot", title="Device boot", description="Run after Android finishes booting."),
        NodeSpec(type=NodeType.TRIGGER, key="screen", title="Screen state", description="React to the display turning on or off."),
        NodeSpec(type=NodeType.TRIGGER, key="battery", title="Battery level", description="Trigger at a battery threshold.", parameters=mapOf("threshold" to "50", "direction" to "at_or_below")),
        NodeSpec(type=NodeType.TRIGGER, key="wifi", title="Wi-Fi state", description="React to Wi-Fi changes."),
        NodeSpec(type=NodeType.TRIGGER, key="bluetooth", title="Bluetooth state", description="React to Bluetooth changes.", capability=Capability.BLUETOOTH),
        NodeSpec(type=NodeType.TRIGGER, key="notification", title="Notification received", description="React to matching notifications.", capability=Capability.NOTIFICATION_LISTENER, parameters=mapOf("contains" to "")),
        NodeSpec(type=NodeType.TRIGGER, key="app_launched", title="Application launched", description="React when an application enters the foreground."),
        NodeSpec(type=NodeType.TRIGGER, key="interval", title="Regular interval", description="Trigger repeatedly.", parameters=mapOf("minutes" to "60")),
        NodeSpec(type=NodeType.TRIGGER, key="time", title="Time of day", description="Trigger at a configured time.", parameters=mapOf("time" to "08:00")),
        NodeSpec(type=NodeType.TRIGGER, key="webhook", title="Webhook", description="Start from an HTTP request."),
        NodeSpec(type=NodeType.TRIGGER, key="shake", title="Shake device", description="Trigger on device motion.")
    )

    val constraints = listOf(
        NodeSpec(type=NodeType.CONSTRAINT, key="time", title="Time window", description="Allow execution only during a time range.", parameters=mapOf("start" to "00:00", "end" to "23:59")),
        NodeSpec(type=NodeType.CONSTRAINT, key="battery", title="Battery condition", description="Compare current battery percentage.", parameters=mapOf("operator" to ">=", "value" to "20")),
        NodeSpec(type=NodeType.CONSTRAINT, key="wifi", title="Wi-Fi condition", description="Require a Wi-Fi state."),
        NodeSpec(type=NodeType.CONSTRAINT, key="bluetooth", title="Bluetooth condition", description="Require a Bluetooth state.", capability=Capability.BLUETOOTH),
        NodeSpec(type=NodeType.CONSTRAINT, key="app", title="Active application", description="Require a selected foreground application."),
        NodeSpec(type=NodeType.CONSTRAINT, key="locked", title="Device locked", description="Check lock state."),
        NodeSpec(type=NodeType.CONSTRAINT, key="location", title="Location condition", description="Require a location condition.", capability=Capability.LOCATION),
        NodeSpec(type=NodeType.CONSTRAINT, key="notification", title="Notification condition", description="Require a matching notification.", capability=Capability.NOTIFICATION_LISTENER),
        NodeSpec(type=NodeType.CONSTRAINT, key="logic", title="Logic", description="Combine conditions with AND / OR / NOT.")
    )

    val actions = listOf(
        NodeSpec(type=NodeType.ACTION, key="notify", title="Show notification", description="Post an Android notification.", capability=Capability.POST_NOTIFICATIONS, parameters=mapOf("title" to "MAC", "text" to "Automation executed")),
        NodeSpec(type=NodeType.ACTION, key="launch_app", title="Launch application", description="Open an installed application.", parameters=mapOf("package" to "")),
        NodeSpec(type=NodeType.ACTION, key="wait", title="Wait", description="Pause execution.", parameters=mapOf("seconds" to "1")),
        NodeSpec(type=NodeType.ACTION, key="variable", title="Set variable", description="Create or update a MAC variable.", parameters=mapOf("name" to "example", "value" to "hello")),
        NodeSpec(type=NodeType.ACTION, key="speak", title="Speak text", description="Use Android text-to-speech.", parameters=mapOf("text" to "Hello from MAC")),
        NodeSpec(type=NodeType.ACTION, key="volume", title="Set media volume", description="Set media stream volume.", parameters=mapOf("percent" to "50")),
        NodeSpec(type=NodeType.ACTION, key="vibrate", title="Vibrate", description="Vibrate the device.", parameters=mapOf("milliseconds" to "150")),
        NodeSpec(type=NodeType.ACTION, key="clipboard", title="Set clipboard", description="Copy text to the clipboard.", parameters=mapOf("text" to "Copied by MAC")),
        NodeSpec(type=NodeType.ACTION, key="http", title="HTTP request", description="Call an HTTPS endpoint.", parameters=mapOf("url" to "https://example.com", "method" to "GET")),
        NodeSpec(type=NodeType.ACTION, key="shell", title="Shell command", description="Execute a command when a privileged adapter is available.", capability=Capability.SHIZUKU, parameters=mapOf("command" to "")),
        NodeSpec(type=NodeType.ACTION, key="ui_click", title="UI interaction", description="Interact with an app through Accessibility.", capability=Capability.ACCESSIBILITY),
        NodeSpec(type=NodeType.ACTION, key="action_block", title="Action block", description="Invoke a reusable action sequence.")
    )
}
