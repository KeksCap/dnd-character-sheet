package com.example.dndhelper.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.GameLogEntry
import com.example.dndhelper.data.LogType
import com.example.dndhelper.tr
import com.example.dndhelper.utils.GameLogManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameLogTab(
    isEn: Boolean,
    logEntries: List<GameLogEntry>,
    onAddLog: (GameLogEntry) -> Unit,
    onClearLog: () -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<LogType?>(null) }

    val filteredEntries = if (selectedFilter != null) {
        logEntries.filter { it.type == selectedFilter }
    } else {
        logEntries
    }

    val chatEntries = filteredEntries.reversed()
    val listState = rememberLazyListState()

    LaunchedEffect(logEntries.size) {
        if (chatEntries.isNotEmpty()) {
            listState.animateScrollToItem(chatEntries.size - 1)
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (logEntries.isNotEmpty()) {
                    SmallFloatingActionButton(
                        onClick = { showClearDialog = true },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = tr("Очистить", "Clear"))
                    }
                }
                FloatingActionButton(
                    onClick = { showNoteDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = tr("Заметка", "Note"))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text(tr("Все", "All"), fontSize = 12.sp) }
                )
                LogType.entries.forEach { type ->
                    val info = getLogTypeInfo(type)
                    if (logEntries.any { it.type == type }) {
                        FilterChip(
                            selected = selectedFilter == type,
                            onClick = { selectedFilter = if (selectedFilter == type) null else type },
                            label = { Text(if (isEn) info.labelEn else info.labelRu, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(getLogTypeColor(type)))
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            if (chatEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Text(tr("Журнал пуст", "Log is empty"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    var lastDateStr = ""
                    items(chatEntries, key = { it.id }) { entry ->
                        val dateStr = formatDate(entry.timestamp)
                        if (dateStr != lastDateStr) {
                            lastDateStr = dateStr
                            DateSeparator(dateStr)
                        }
                        GameLogEntryCard(entry = entry, isEn = isEn)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
    if (showNoteDialog) NoteDialog(onDismiss = { showNoteDialog = false }, onAdd = onAddLog)
    if (showClearDialog) ClearDialog(logSize = logEntries.size, onDismiss = { showClearDialog = false }, onClear = onClearLog)
}

@Composable
fun GameLogEntryCard(entry: GameLogEntry, isEn: Boolean) {
    val typeColor = getLogTypeColor(entry.type)
    val (icon, _) = getLogTypeIcon(entry.type)
    val message = if (isEn) entry.messageEn else entry.messageRu
    val timeStr = formatTime(entry.timestamp)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = typeColor.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = typeColor)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = timeStr, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun DateSeparator(dateStr: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = dateStr, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun NoteDialog(onDismiss: () -> Unit, onAdd: (GameLogEntry) -> Unit) {
    var noteText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Новая заметка", "New Note")) },
        text = { OutlinedTextField(value = noteText, onValueChange = { noteText = it }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = { if (noteText.isNotBlank()) { onAdd(GameLogManager.createNote(noteText.trim())); onDismiss() } }) { Text(tr("Записать", "Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Отмена", "Cancel")) } }
    )
}

@Composable
fun ClearDialog(logSize: Int, onDismiss: () -> Unit, onClear: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Очистить журнал?", "Clear log?")) },
        confirmButton = { Button(onClick = { onClear(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(tr("Очистить", "Clear")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Отмена", "Cancel")) } }
    )
}

private fun getLogTypeColor(type: LogType): Color = when (type) {
    LogType.DAMAGE -> Color(0xFFE53935)
    LogType.HEAL -> Color(0xFF43A047)
    LogType.POTION -> Color(0xFF00897B)
    LogType.SPELL_SLOT -> Color(0xFF5E35B1)
    LogType.REST -> Color(0xFF1E88E5)
    LogType.CONDITION -> Color(0xFFFF8F00)
    LogType.DICE_ROLL -> Color(0xFFF4511E)
    LogType.NOTE -> Color(0xFF546E7A)
}

private fun getLogTypeIcon(type: LogType): Pair<ImageVector, String> = when (type) {
    LogType.DAMAGE -> Icons.Default.HeartBroken to ""
    LogType.HEAL -> Icons.Default.Favorite to ""
    LogType.POTION -> Icons.Default.LocalPharmacy to ""
    LogType.SPELL_SLOT -> Icons.Default.AutoFixHigh to ""
    LogType.REST -> Icons.Default.NightsStay to ""
    LogType.CONDITION -> Icons.Default.FlashOn to ""
    LogType.DICE_ROLL -> Icons.Default.Casino to ""
    LogType.NOTE -> Icons.Default.StickyNote2 to ""
}

private data class LogTypeInfo(val labelRu: String, val labelEn: String)
private fun getLogTypeInfo(type: LogType): LogTypeInfo = when (type) {
    LogType.DAMAGE -> LogTypeInfo("Урон", "Damage")
    LogType.HEAL -> LogTypeInfo("Лечение", "Heal")
    LogType.POTION -> LogTypeInfo("Зелья", "Potions")
    LogType.SPELL_SLOT -> LogTypeInfo("Заклин.", "Spells")
    LogType.REST -> LogTypeInfo("Отдых", "Rest")
    LogType.CONDITION -> LogTypeInfo("Состояния", "Conditions")
    LogType.DICE_ROLL -> LogTypeInfo("Кубики", "Dice")
    LogType.NOTE -> LogTypeInfo("Заметки", "Notes")
}

private fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
private fun formatDate(timestamp: Long): String = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
