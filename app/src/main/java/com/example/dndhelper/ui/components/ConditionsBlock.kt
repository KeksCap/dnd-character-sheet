package com.example.dndhelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.tr
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConditionsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    isEn: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    val conditionsRu = listOf(
        "Ослеплен", "Очарован", "Глухота", "Испуган", "Схвачен",
        "Недееспособен", "Невидимый", "Парализован", "Окаменел",
        "Отравлен", "Сбит с ног", "Опутан", "Ошеломлен", "Без сознания"
    )

    val conditionsEn = listOf(
        "Blinded", "Charmed", "Deafened", "Frightened", "Grappled",
        "Incapacitated", "Invisible", "Paralyzed", "Petrified",
        "Poisoned", "Prone", "Restrained", "Stunned", "Unconscious"
    )

    val allConditions = if (isEn) conditionsEn else conditionsRu
    
    val context = LocalContext.current
    val conditionsData = remember {
        try {
            val inputStream = context.assets.open("conditions_info.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.type
            Gson().fromJson<Map<String, Map<String, Map<String, String>>>>(reader, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun getConditionId(name: String): String {
        return when (name.lowercase().replace("ё", "е")) {
            "ослеплен", "blinded", "ослепленный" -> "blinded"
            "очарован", "charmed" -> "charmed"
            "глухота", "deafened", "оглохший" -> "deafened"
            "испуган", "frightened", "испуганный" -> "frightened"
            "схвачен", "grappled", "схваченный" -> "grappled"
            "недееспособен", "incapacitated", "недееспособный" -> "incapacitated"
            "невидимый", "invisible" -> "invisible"
            "парализован", "paralyzed", "парализованный" -> "paralyzed"
            "окаменел", "petrified", "окаменевший" -> "petrified"
            "отравлен", "poisoned", "отравленный" -> "poisoned"
            "сбит с ног", "prone", "лежащий" -> "prone"
            "опутан", "restrained", "опутанный" -> "restrained"
            "ошеломлен", "stunned", "ошеломленный" -> "stunned"
            "без сознания", "unconscious" -> "unconscious"
            "истощение", "exhaustion" -> "exhaustion"
            else -> name.lowercase()
        }
    }

    fun showInfo(name: String) {
        val id = getConditionId(name)
        val edition = if (character.use2024Rules) "2024" else "2014"
        val lang = if (isEn) "en" else "ru"
        val notFound = if (isEn) "Description not found" else "Описание не найдено"
        val desc = conditionsData[id]?.get(edition)?.get(lang) ?: notFound
        infoDialogContent = name to desc
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tr("Состояния и Эффекты", "Conditions & Effects"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Переключатель редакций
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (character.use2024Rules) "2024 (SRD 5.2)" else "2014 (SRD 5.1)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Switch(
                            checked = character.use2024Rules,
                            onCheckedChange = { onCharacterChange(character.copy(use2024Rules = it)) },
                            modifier = Modifier.scale(0.5f).height(20.dp)
                        )
                    }
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Condition", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Ряд с активными состояниями
            if (character.activeConditions.isEmpty() && character.exhaustionLevel == 0) {
                Text(
                    text = tr("Нет активных состояний", "No active conditions"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Истощение (если > 0)
                    if (character.exhaustionLevel > 0) {
                        ConditionChip(
                            label = "${tr("Истощение", "Exhaustion")} ${character.exhaustionLevel}",
                            onRemove = { onCharacterChange(character.copy(exhaustionLevel = 0)) },
                            onInfo = { showInfo("Истощение") },
                            isWarning = true
                        )
                    }

                    // Обычные состояния
                    character.activeConditions.forEach { condition ->
                        ConditionChip(
                            label = condition,
                            onRemove = {
                                val newList = character.activeConditions.toMutableList()
                                newList.remove(condition)
                                onCharacterChange(character.copy(activeConditions = newList))
                            },
                            onInfo = { showInfo(condition) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Селектор истощения
            Column {
                Text(
                    text = tr("Уровень Истощения:", "Exhaustion Level:"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..6).forEach { level ->
                        val isSelected = character.exhaustionLevel == level
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onCharacterChange(character.copy(exhaustionLevel = level)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                if (character.exhaustionLevel > 0) {
                    val effectRu = when(character.exhaustionLevel) {
                        1 -> "Помеха при проверках характеристик"
                        2 -> "Скорость уменьшается вдвое"
                        3 -> "Помеха при бросках атаки и спасбросках"
                        4 -> "Максимум хитов уменьшается вдвое"
                        5 -> "Скорость становится 0"
                        6 -> "Смерть"
                        else -> ""
                    }
                    val effectEn = when(character.exhaustionLevel) {
                        1 -> "Disadvantage on Ability Checks"
                        2 -> "Speed halved"
                        3 -> "Disadvantage on Attack Rolls and Saving Throws"
                        4 -> "Hit Point maximum halved"
                        5 -> "Speed reduced to 0"
                        6 -> "Death"
                        else -> ""
                    }
                    Text(
                        text = tr("Эффект: $effectRu", "Effect: $effectEn"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp).clickable { showInfo("Истощение") }
                    )
                }
            }
        }
    }

    if (infoDialogContent != null) {
        val (name, desc) = infoDialogContent!!
        AlertDialog(
            onDismissRequest = { infoDialogContent = null },
            title = { Text(name, fontWeight = FontWeight.Bold) },
            text = {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    Text(text = formatMarkdown(desc))
                }
            },
            confirmButton = {
                TextButton(onClick = { infoDialogContent = null }) {
                    Text(tr("Ясно", "Got it"))
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(tr("Состояния", "Conditions")) },
            text = {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(scrollState)) {
                    allConditions.forEach { condition ->
                        val isActive = character.activeConditions.contains(condition)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newList = character.activeConditions.toMutableList()
                                    if (isActive) newList.remove(condition) else newList.add(condition)
                                    onCharacterChange(character.copy(activeConditions = newList))
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isActive, onCheckedChange = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(condition)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(tr("Готово", "Done"))
                }
            }
        )
    }
}

@Composable
fun ConditionChip(
    label: String,
    onRemove: () -> Unit,
    onInfo: () -> Unit,
    isWarning: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWarning) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.clickable { onInfo() }
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.Help,
                contentDescription = "Info",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onInfo() },
                tint = if (isWarning) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onRemove() },
                tint = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

fun formatMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val boldRegex = "\\*\\*_([^*]+)_\\*\\*".toRegex()
        val italicRegex = "\\*([^*]+)\\*".toRegex()
        
        var lastIndex = 0
        
        // Поиск всех вхождений жирного+курсива из SRD (обычно там так: **_Title._**)
        val matches = "(\\*\\*_([^*]+)_\\*\\*|\\*([^*]+)\\*)".toRegex().findAll(text)
        
        for (match in matches) {
            // Текст до совпадения
            append(text.substring(lastIndex, match.range.first))
            
            val content = match.groupValues[2].takeIf { it.isNotEmpty() } ?: match.groupValues[3]
            
            if (match.value.startsWith("**_")) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                    append(content)
                }
            } else {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(content)
                }
            }
            
            lastIndex = match.range.last + 1
        }
        
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
