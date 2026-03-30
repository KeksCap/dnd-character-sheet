package com.example.dndhelper.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.Spell
import com.example.dndhelper.data.SpellInfo
import com.example.dndhelper.utils.schoolColor
import com.example.dndhelper.utils.sourceName

@Composable
fun SpellsDirectoryTab(
    isEn: Boolean,
    spells: List<Spell>,
    classSpells: Map<String, List<String>>,
    language: String,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var selectedSpell by remember { mutableStateOf<SpellInfo?>(null) }
    var selectedClass by remember { mutableStateOf("Все") }
    var searchQuery by remember { mutableStateOf("") }

    // Состояние сортировки (0 - нет, 1 - по возрастанию, 2 - по убыванию)
    var sortOrder by remember { mutableIntStateOf(0) }

    val isRussian = !isEn

    // Умный фильтр по классу и тексту
    val filteredSpells = spells.filter { spell ->
        val matchesClass = if (selectedClass == "Все") {
            true
        } else {
            val allowedSpells = classSpells[selectedClass] ?: emptyList()
            val spellNameEn = spell.en?.name?.trim()?.lowercase() ?: ""
            allowedSpells.any { it.trim().lowercase() == spellNameEn }
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            val spellName = (if (isRussian) spell.ru?.name else spell.en?.name) ?: ""
            spellName.contains(searchQuery, ignoreCase = true)
        }

        matchesClass && matchesSearch
    }

    // Логика сортировки по уровню
    val sortedSpells = when (sortOrder) {
        1 -> filteredSpells.sortedBy { spell ->
            val lvl = if (isRussian) spell.ru?.level else spell.en?.level
            lvl.toString().toIntOrNull() ?: 0 // Заговоры (0) будут первыми
        }
        2 -> filteredSpells.sortedByDescending { spell ->
            val lvl = if (isRussian) spell.ru?.level else spell.en?.level
            lvl.toString().toIntOrNull() ?: 0 // Заклинания 9 уровня будут первыми
        }
        else -> filteredSpells // Как в оригинале
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (isRussian) "Справочник заклинаний" else "Spells Directory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Строка поиска и кнопка сортировки в одном ряду
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (isRussian) "Поиск заклинания..." else "Search spell...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                modifier = Modifier.weight(1f), // Поиск занимает всё свободное место
                singleLine = true
            )

            // КНОПКА СОРТИРОВКИ
            IconButton(
                onClick = { sortOrder = (sortOrder + 1) % 3 },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                val sortIcon = when (sortOrder) {
                    1 -> Icons.Default.ArrowUpward // По возрастанию
                    2 -> Icons.Default.ArrowDownward // По убыванию
                    else -> Icons.Default.Sort // По умолчанию
                }
                Icon(sortIcon, contentDescription = "Сортировка", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            val classList = listOf("Все") + classSpells.keys.toList()
            items(classList) { className ->
                val isSelected = selectedClass == className
                Button(
                    onClick = { selectedClass = className },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(className)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sortedSpells) { spell ->
                val finalInfo = if (isRussian) spell.ru else spell.en

                if (finalInfo != null) {
                    val schoolCol = schoolColor(finalInfo.school)
                    ListItem(
                        headlineContent = { Text(finalInfo.name ?: "Без названия", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            val levelText = if (finalInfo.level == "0") {
                                if (isRussian) "Заговор" else "Cantrip"
                            } else {
                                if (isRussian) "${finalInfo.level} уровень" else "Level ${finalInfo.level}"
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                // Чип Школы
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = schoolCol.copy(alpha = 0.15f)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Canvas(modifier = Modifier.size(7.dp)) { drawCircle(color = schoolCol) }
                                        Spacer(Modifier.width(4.dp))
                                        Text(finalInfo.school ?: "", fontSize = 11.sp, color = schoolCol, fontWeight = FontWeight.Bold)
                                    }
                                }
                                // Уровень
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(levelText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        },
                        trailingContent = {
                            Text(spell.en?.source ?: "", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        },
                        modifier = Modifier.clickable { selectedSpell = finalInfo }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    selectedSpell?.let { spell ->
        val schoolCol = schoolColor(spell.school)
        AlertDialog(
            onDismissRequest = { selectedSpell = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spell.name ?: "", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        val isFavorite = character.knownSpells.contains(spell)
                        IconButton(onClick = {
                            val updatedSpells = if (isFavorite) character.knownSpells - spell else character.knownSpells + spell
                            onCharacterChange(character.copy(knownSpells = updatedSpells))
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                            )
                        }
                    }
                    // Чипы: школа и источник
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        // Школа магии
                        if (!spell.school.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = schoolCol
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text(spell.school, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Источник или "Гомебрю"
                        if (!spell.source.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    sourceName(spell.source),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(if (isRussian) "Время: ${spell.castingTime}" else "Casting Time: ${spell.castingTime}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Дистанция: ${spell.range}" else "Range: ${spell.range}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Компоненты: ${spell.components}" else "Components: ${spell.components}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Длительность: ${spell.duration}" else "Duration: ${spell.duration}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    val cleanText = spell.text?.replace("<br>", "\n\n") ?: (if (isRussian) "Описание отсутствует." else "No description available.")
                    Text(cleanText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSpell = null }) {
                    Text(if (isRussian) "Закрыть" else "Close")
                }
            }
        )
    }
}
