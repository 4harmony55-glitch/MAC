package com.mac.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mac.app.data.MacroRepository
import com.mac.app.domain.*
import com.mac.app.runtime.ExecutionEngine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MacTheme {
                MACApp(this)
            }
        }
    }
}

@Composable
private fun MacTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}

@Composable
private fun MACApp(activity: ComponentActivity) {
    val repo = remember { MacroRepository(activity.applicationContext) }
    val engine = remember { ExecutionEngine(activity.applicationContext) }
    val inventory = remember { CapabilityInventory(activity.applicationContext) }
    val scope = rememberCoroutineScope()
    var macros by remember { mutableStateOf(repo.load()) }
    var editor by remember { mutableStateOf<Macro?>(null) }
    var logs by remember { mutableStateOf(listOf<ExecutionEvent>()) }
    var showLog by remember { mutableStateOf(false) }

    fun run(m: Macro) {
        showLog = true
        logs = listOf(ExecutionEvent(level = ExecutionEvent.Level.INFO, message = "Queued ${m.name}"))
        scope.launch {
            engine.run(m) { e ->
                logs = logs + e
            }
        }
    }

    when {
        showLog -> LogScreen(logs) { showLog = false }
        editor != null -> EditorScreen(
            initial = editor!!,
            inventory = inventory,
            onBack = { editor = null },
            onSave = { m ->
                repo.upsert(m)
                macros = repo.load()
                editor = null
            },
            onRun = { run(it) }
        )
        else -> HomeScreen(
            macros = macros,
            onNew = { editor = Macro(name = "New macro") },
            onEdit = { editor = it },
            onRun = { run(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    macros: List<Macro>,
    onNew: () -> Unit,
    onEdit: (Macro) -> Unit,
    onRun: (Macro) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MAC", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNew) {
                        Icon(Icons.Default.Add, "New macro")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNew) {
                Icon(Icons.Default.Add, "New macro")
            }
        }
    ) { p ->
        if (macros.isEmpty()) {
            EmptyState(Modifier.padding(p).fillMaxSize(), onNew)
        } else {
            LazyColumn(
                Modifier
                    .padding(p)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Automations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                items(macros, key = { it.id }) { m ->
                    MacroCard(m, onEdit, onRun)
                }
                item {
                    Spacer(Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onNew: () -> Unit) = Column(
    modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Icon(Icons.Default.Bolt, null, Modifier.size(58.dp))
    Spacer(Modifier.height(16.dp))
    Text("Build your first automation", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text("Trigger → constraints → actions", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(22.dp))
    Button(onClick = onNew) { Text("Create macro") }
}

@Composable
private fun MacroCard(m: Macro, onEdit: (Macro) -> Unit, onRun: (Macro) -> Unit) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { onEdit(m) },
    shape = RoundedCornerShape(20.dp)
) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (m.enabled) "Enabled" else "Disabled", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onRun(m) }) { Icon(Icons.Default.PlayArrow, "Run") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("${m.triggers.size} triggers")
            Chip("${m.constraints.size} constraints")
            Chip("${m.actions.size} actions")
        }
    }
}

@Composable
private fun Chip(text: String) = Surface(
    shape = RoundedCornerShape(50),
    color = MaterialTheme.colorScheme.surfaceVariant
) {
    Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(
    initial: Macro,
    inventory: CapabilityInventory,
    onBack: () -> Unit,
    onSave: (Macro) -> Unit,
    onRun: (Macro) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var enabled by remember { mutableStateOf(initial.enabled) }
    var triggers by remember { mutableStateOf(initial.triggers) }
    var constraints by remember { mutableStateOf(initial.constraints) }
    var actions by remember { mutableStateOf(initial.actions) }
    var picker by remember { mutableStateOf<NodeType?>(null) }
    var editingNode by remember { mutableStateOf<Pair<NodeType, Int>?>(null) }

    fun commit(): Macro = initial.copy(
        name = name.ifBlank { "Untitled macro" },
        enabled = enabled,
        triggers = triggers,
        constraints = constraints,
        actions = actions,
        updatedAt = System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                title = { Text("Macro builder") },
                actions = {
                    IconButton(onClick = { onRun(commit()) }) { Icon(Icons.Default.PlayArrow, "Run") }
                    IconButton(onClick = { onSave(commit()) }) { Icon(Icons.Default.Check, "Save") }
                }
            )
        }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Section("TRIGGERS", triggers, { picker = NodeType.TRIGGER }, { idx -> triggers = triggers.toMutableList().also { it.removeAt(idx) } }, { idx -> editingNode = NodeType.TRIGGER to idx })
            Section("CONSTRAINTS", constraints, { picker = NodeType.CONSTRAINT }, { idx -> constraints = constraints.toMutableList().also { it.removeAt(idx) } }, { idx -> editingNode = NodeType.CONSTRAINT to idx })
            Section("ACTIONS", actions, { picker = NodeType.ACTION }, { idx -> actions = actions.toMutableList().also { it.removeAt(idx) } }, { idx -> editingNode = NodeType.ACTION to idx })
            Button(onClick = { onSave(commit()) }, Modifier.fillMaxWidth()) { Text("Save macro") }
            Spacer(Modifier.height(70.dp))
        }
    }

    picker?.let { type ->
        NodePicker(type, inventory, { picker = null }) { node ->
            when (type) {
                NodeType.TRIGGER -> triggers = triggers + node
                NodeType.CONSTRAINT -> constraints = constraints + node
                NodeType.ACTION -> actions = actions + node
            }
            picker = null
        }
    }

    editingNode?.let { (type, index) ->
        val currentNode = when (type) {
            NodeType.TRIGGER -> triggers.getOrNull(index)
            NodeType.CONSTRAINT -> constraints.getOrNull(index)
            NodeType.ACTION -> actions.getOrNull(index)
        }
        currentNode?.let { node ->
            NodeParameterEditor(
                node = node,
                onDismiss = { editingNode = null },
                onSave = { updated ->
                    when (type) {
                        NodeType.TRIGGER -> triggers = triggers.toMutableList().also { it[index] = updated }
                        NodeType.CONSTRAINT -> constraints = constraints.toMutableList().also { it[index] = updated }
                        NodeType.ACTION -> actions = actions.toMutableList().also { it[index] = updated }
                    }
                    editingNode = null
                }
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    nodes: List<NodeSpec>,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onAdd) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
        if (nodes.isEmpty()) {
            Surface(shape = RoundedCornerShape(15.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Text("No ${title.lowercase()} configured", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            nodes.forEachIndexed { i, n ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.weight(1f).clickable { onEdit(i) }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(n.title, fontWeight = FontWeight.SemiBold)
                            Text(n.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                    }
                    IconButton(onClick = { onDelete(i) }) { Icon(Icons.Default.Delete, "Delete") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodePicker(
    type: NodeType,
    inventory: CapabilityInventory,
    onDismiss: () -> Unit,
    onPick: (NodeSpec) -> Unit
) {
    var q by remember { mutableStateOf("") }
    val core = when (type) {
        NodeType.TRIGGER -> BuiltInCatalog.triggers
        NodeType.CONSTRAINT -> BuiltInCatalog.constraints
        NodeType.ACTION -> BuiltInCatalog.actions
    }
    val inventoryNodes = inventory.entries.filter { it.kind == type }.map {
        NodeSpec(type = type, key = it.id, title = it.name, description = "Catalog capability — adapter implementation tracked by MAC.")
    }
    val base = (core + inventoryNodes).distinctBy { it.key }
    val filtered = base.filter { ("${it.title} ${it.description}").contains(q, true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxHeight(.9f).padding(horizontal = 16.dp)) {
            Text("Add ${type.name.lowercase()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search") }
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(filtered) { n ->
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(n) }
                    ) {
                        ListItem(
                            headlineContent = { Text(n.title) },
                            supportingContent = { Text(n.description) },
                            leadingContent = { Icon(Icons.Default.Bolt, null) },
                            trailingContent = { if (n.capability != Capability.NONE) Icon(Icons.Default.Tune, "Capability") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeParameterEditor(
    node: NodeSpec,
    onDismiss: () -> Unit,
    onSave: (NodeSpec) -> Unit
) {
    var params by remember { mutableStateOf(node.parameters) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxHeight(.85f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(node.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(node.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            node.parameters.forEach { (key, initial) ->
                var value by remember(initial) { mutableStateOf(params[key] ?: initial) }
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        params = params + mapOf(key to it)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    label = { Text(key.replace('_', ' ').replaceFirstChar { it.uppercase() }) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = if (initial.toLongOrNull() != null) KeyboardType.Number else KeyboardType.Text)
                )
            }
            Button(onClick = { onSave(node.copy(parameters = params)) }, Modifier.fillMaxWidth()) {
                Text("Save settings")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogScreen(logs: List<ExecutionEvent>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                title = { Text("Execution log") }
            )
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Live execution trace", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }
            items(logs) { e ->
                Text(
                    text = "${e.level.name.padEnd(7)}  ${e.message}",
                    modifier = Modifier.padding(vertical = 6.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
