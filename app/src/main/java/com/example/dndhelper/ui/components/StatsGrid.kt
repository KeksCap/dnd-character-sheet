package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.ui.models.AbilityScore
import com.example.dndhelper.tr
import com.example.dndhelper.utils.trStat

import com.example.dndhelper.ui.models.DiceRollData
import com.example.dndhelper.ui.models.RollType

@Composable
fun StatsGrid(
    stats: List<AbilityScore>,
    isEn: Boolean,
    profBonus: Int,
    hasDisadvantageOnChecks: Boolean,
    onStatsChange: (List<AbilityScore>) -> Unit,
    onRollRequest: (DiceRollData) -> Unit
) {
    val attributesTitle = tr("Характеристики", "Attributes")

    Column {
        Text(attributesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        stats.chunked(2).forEach { rowData ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowData.forEach { statData ->
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            stat = statData,
                            isEn = isEn,
                            profBonus = profBonus,
                            hasDisadvantage = hasDisadvantageOnChecks,
                            onValueChange = { newValue ->
                                onStatsChange(stats.map { if (it.name == statData.name) it.copy(baseScore = newValue) else it })
                            },
                            onSkillProficiencyChange = { skillName, newProf ->
                                onStatsChange(stats.map { 
                                    if (it.name == statData.name) {
                                        val newSkills = it.skillProficiencies.toMutableMap()
                                        newSkills[skillName] = newProf
                                        it.copy(skillProficiencies = newSkills)
                                    } else it 
                                })
                            },
                            onRollRequest = onRollRequest
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    stat: AbilityScore, 
    isEn: Boolean, 
    profBonus: Int, 
    hasDisadvantage: Boolean, 
    onValueChange: (Int) -> Unit,
    onSkillProficiencyChange: (String, Int) -> Unit,
    onRollRequest: (DiceRollData) -> Unit
) {
    val modifier = (stat.baseScore - 10) / 2
    val translatedTitle = trStat(stat.name, isEn) // Перевод заголовка (не Composable больше)

    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { 
                        onRollRequest(DiceRollData(
                            title = translatedTitle,
                            rollType = RollType.AbilityCheck,
                            baseModifier = modifier,
                            icon = stat.icon
                        ))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(stat.icon, null, tint = Color(0xFF6750A4), modifier = Modifier.size(24.dp))
                    Text(translatedTitle, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onValueChange(stat.baseScore - 1) }) { Text("-", fontSize = 24.sp) }
                Text("${stat.baseScore}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onValueChange(stat.baseScore + 1) }) { Text("+", fontSize = 24.sp) }
            }

            Surface(
                color = Color(0xFFEADDFF), 
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { 
                    onRollRequest(DiceRollData(
                        title = translatedTitle,
                        rollType = RollType.AbilityCheck,
                        baseModifier = modifier,
                        icon = stat.icon
                    ))
                }
            ) {
                Text(if (modifier >= 0) "+$modifier" else "$modifier", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            stat.skills.forEach { skill ->
                val state = stat.skillProficiencies[skill] ?: 0
                val totalSkillBonus = modifier + (state * profBonus)
                val skillName = trStat(skill, isEn)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onRollRequest(DiceRollData(
                                title = skillName,
                                rollType = RollType.SkillCheck,
                                baseModifier = modifier,
                                proficiencyBonus = state * profBonus,
                                isProficient = state > 0,
                                expertise = state == 2
                            ))
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(state) {
                            1 -> Icons.Default.Check
                            2 -> Icons.Default.Star
                            else -> Icons.Default.RadioButtonChecked // Используем крашеную в серый для пустоты
                        },
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                onSkillProficiencyChange(skill, (state + 1) % 3)
                            },
                        tint = if (state > 0) Color(0xFF6750A4) else Color.LightGray
                    )

                    Text(
                        text = skillName,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = if (state > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasDisadvantage) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Disadvantage",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            text = if (totalSkillBonus >= 0) "+$totalSkillBonus" else "$totalSkillBonus",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (hasDisadvantage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
