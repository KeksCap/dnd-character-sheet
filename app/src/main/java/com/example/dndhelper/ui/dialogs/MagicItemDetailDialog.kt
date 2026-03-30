package com.example.dndhelper.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.MagicItem
import com.example.dndhelper.tr

@Composable
fun MagicItemDetailDialog(
    item: MagicItem,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    onDismiss: () -> Unit,
    isEn: Boolean
) {
    val name = if (isEn) item.nameEn else item.nameRu
    val type = if (isEn) item.typeEn else item.typeRu
    val subtype = if (isEn) item.subtypeEn else item.subtypeRu
    val description = if (isEn) item.descriptionEn else item.descriptionRu
    val isFavorite = character.magicItems.any { it.slug == item.slug && it.nameEn == item.nameEn }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        val updated = if (isFavorite) {
                            character.magicItems.filterNot { it.slug == item.slug && it.nameEn == item.nameEn }
                        } else {
                            character.magicItems + item
                        }
                        onCharacterChange(character.copy(magicItems = updated))
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }
                val typeText = buildString {
                    append(type)
                    if (!subtype.isNullOrBlank()) append(" ($subtype)")
                    if (!item.rarity.isNullOrBlank()) append(", ${item.rarity}")
                }
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (item.attunement?.required == true) {
                    val cond = item.attunement.condition ?: (if (isEn) "Required" else "Требуется")
                    val isAttuned = item.isAttuned

                    Surface(
                        onClick = {
                            val updatedItems = character.magicItems.map {
                                if (it.slug == item.slug && it.nameEn == item.nameEn) {
                                    it.copy(isAttuned = !isAttuned)
                                } else it
                            }
                            onCharacterChange(character.copy(magicItems = updatedItems))
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAttuned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Bolt,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isAttuned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${tr("Настроено:", "Attuned:")} ${if (isAttuned) tr("Да", "Yes") else tr("Нет", "No")} ($cond)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAttuned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val cleanText = description?.replace("<br>", "\n\n") ?: (if (isEn) "No description available." else "Описание отсутствует.")
                Text(cleanText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Закрыть", "Close"))
            }
        }
    )
}
