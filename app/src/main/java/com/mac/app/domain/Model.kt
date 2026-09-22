package com.mac.app.domain

import java.util.UUID

enum class NodeType { TRIGGER, CONSTRAINT, ACTION }
enum class Capability { NONE, POST_NOTIFICATIONS, ACCESSIBILITY, LOCATION, NOTIFICATION_LISTENER, WRITE_SETTINGS, BLUETOOTH, CAMERA, MICROPHONE, SMS, CALL_LOG, SHIZUKU }

data class NodeSpec(
    val id: String = UUID.randomUUID().toString(),
    val type: NodeType,
    val key: String,
    val title: String,
    val description: String,
    val parameters: Map<String, String> = emptyMap(),
    val capability: Capability = Capability.NONE
)

data class Macro(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val triggers: List<NodeSpec> = emptyList(),
    val constraints: List<NodeSpec> = emptyList(),
    val actions: List<NodeSpec> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExecutionEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val level: Level,
    val message: String
) { enum class Level { INFO, SUCCESS, WARNING, ERROR } }
