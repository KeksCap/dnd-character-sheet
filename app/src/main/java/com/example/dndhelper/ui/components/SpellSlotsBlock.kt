package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.tr

@Composable
fun SpellSlotsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var showAddSlotsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = tr("ЯЧЕЙКИ ЗАКЛИНАНИЙ", "SPELL SLOTS"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val activeLevels = (1..9).filter { character.maxSpellSlots[it] > 0 }
                
                if (activeLevels.isEmpty()) {
                    Text(
                        tr("Ячейки не настроены.", "Slots not configured."),
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    activeLevels.forEach { lvl ->
                        val max = character.maxSpellSlots[lvl]
                        val current = character.currentSpellSlots[lvl]

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Уровень $lvl", "Level $lvl"),
                                modifier = Modifier.width(80.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    val newMax = character.maxSpellSlots.toMutableList().apply { this[lvl] = (this[lvl] - 1).coerceAtLeast(0) }
                                    val newCurrent = character.currentSpellSlots.toMutableList().apply { this[lvl] = this[lvl].coerceAtMost(newMax[lvl]) }
                                    onCharacterChange(character.copy(maxSpellSlots = newMax, currentSpellSlots = newCurrent))
                                },
                                modifier = Modifier.size(30.dp)
                            ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }

                            Text("$max", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)

                            IconButton(
                                onClick = {
                                    val newMax = character.maxSpellSlots.toMutableList().apply { this[lvl] = (this[lvl] + 1).coerceAtMost(10) }
                                    onCharacterChange(character.copy(maxSpellSlots = newMax))
                                },
                                modifier = Modifier.size(30.dp)
                            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }

                            Spacer(Modifier.width(12.dp))

                            Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                                for (i in 1..max) {
                                    val isAvailable = i <= current
                                    Icon(
                                        imageVector = if (isAvailable) Icons.Default.Circle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(2.dp)
                                            .clickable {
                                                val newCurrent = character.currentSpellSlots.toMutableList().apply {
                                                    this[lvl] = if (isAvailable) i - 1 else i
                                                }
                                                onCharacterChange(character.copy(currentSpellSlots = newCurrent))
                                            },
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { showAddSlotsDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("ДОБАВИТЬ УРОВЕНЬ", "ADD LEVEL"))
                }
            }
        }
    }

    if (showAddSlotsDialog) {
        AlertDialog(
            onDismissRequest = { showAddSlotsDialog = false },
            title = { Text(tr("Добавить ячейки", "Add Spell Slots")) },
            text = {
                Column {
                    Text(tr("Выберите уровень заклинаний:", "Select spell level:"))
                    Spacer(Modifier.height(8.dp))
                    (1..9).chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            row.forEach { lvl ->
                                val isAdded = character.maxSpellSlots[lvl] > 0
                                Button(
                                    onClick = {
                                        val newMax = character.maxSpellSlots.toMutableList().apply { this[lvl] = 1 }
                                        val newCurrent = character.currentSpellSlots.toMutableList().apply { this[lvl] = 1 }
                                        onCharacterChange(character.copy(maxSpellSlots = newMax, currentSpellSlots = newCurrent))
                                        showAddSlotsDialog = false
                                    },
                                    modifier = Modifier.padding(4.dp),
                                    enabled = !isAdded,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAdded) Color.Gray else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("$lvl")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddSlotsDialog = false }) { Text(tr("Закрыть", "Close")) }
            }
        )
    }
}
