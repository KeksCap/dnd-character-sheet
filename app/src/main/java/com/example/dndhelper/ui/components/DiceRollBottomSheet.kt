package com.example.dndhelper.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.tr
import com.example.dndhelper.ui.models.AdvantageMode
import com.example.dndhelper.ui.models.DiceRollData
import com.example.dndhelper.ui.models.RollOutcome
import com.example.dndhelper.ui.models.RollType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollBottomSheet(
    rollData: DiceRollData,
    onDismiss: () -> Unit
) {
    // --- ПЕРЕВОДЫ (Кэшируем в начале, чтобы избежать ошибок @Composable) ---
    val labelDisadvantage = tr("Помеха", "Disadv.")
    val labelNormal = tr("Норма", "Normal")
    val labelAdvantage = tr("Преимущ.", "Advant.")
    val labelCritSuccess = tr("КРИТИЧЕСКИЙ УСПЕХ!", "CRITICAL SUCCESS!")
    val labelCritFailure = tr("КРИТИЧЕСКИЙ ПРОВАЛ!", "CRITICAL FAIL!")
    val labelRollAgain = tr("Бросить еще раз", "Roll Again")
    val labelTapToRoll = tr("Нажми на кубик", "Tap the die")
    
    val scope = rememberCoroutineScope()
    var advantageMode by remember { mutableStateOf(AdvantageMode.None) }
    var rollOutcome by remember { mutableStateOf<RollOutcome?>(null) }
    var isRolling by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "DiceRotation")
    val angleX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "X"
    )
    val angleY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "Y"
    )

    var faceNumbers by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6)) }
    LaunchedEffect(isRolling) {
        if (isRolling) {
            while (isRolling) {
                faceNumbers = List(6) { Random.nextInt(1, rollData.diceSides + 1) }
                delay(80) 
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rollData.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            if (rollData.diceSides == 20 && rollData.diceCount == 1) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AdvantageButton(
                        label = labelDisadvantage,
                        selected = advantageMode == AdvantageMode.Disadvantage,
                        onClick = { advantageMode = AdvantageMode.Disadvantage; rollOutcome = null }
                    )
                    AdvantageButton(
                        label = labelNormal,
                        selected = advantageMode == AdvantageMode.None,
                        onClick = { advantageMode = AdvantageMode.None; rollOutcome = null }
                    )
                    AdvantageButton(
                        label = labelAdvantage,
                        selected = advantageMode == AdvantageMode.Advantage,
                        onClick = { advantageMode = AdvantageMode.Advantage; rollOutcome = null }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (isRolling) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                    val density = LocalDensity.current.density

                    Canvas(modifier = Modifier.size(55.dp)) {
                        val w = size.width
                        val h = size.height
                        val halfSize = w / 2

                        val radX = Math.toRadians(angleX.toDouble())
                        val sX = sin(radX).toFloat()
                        val cX = cos(radX).toFloat()
                        val radY = Math.toRadians(angleY.toDouble())
                        val sY = sin(radY).toFloat()
                        val cY = cos(radY).toFloat()

                        val rawVertices = listOf(
                            Triple(-halfSize, -halfSize, halfSize), Triple(halfSize, -halfSize, halfSize),
                            Triple(halfSize, halfSize, halfSize), Triple(-halfSize, halfSize, halfSize),
                            Triple(-halfSize, -halfSize, -halfSize), Triple(halfSize, -halfSize, -halfSize),
                            Triple(halfSize, halfSize, -halfSize), Triple(-halfSize, halfSize, -halfSize)
                        )

                        val projected = rawVertices.map { (vx, vy, vz) ->
                            val y1 = vy * cX - vz * sX
                            val z1 = vy * sX + vz * cX
                            val x2 = vx * cY + z1 * sY
                            val z2 = -vx * sY + z1 * cY
                            val perspective = 300f
                            val factor = perspective / (perspective - z2)
                            Triple(x2 * factor + halfSize, y1 * factor + halfSize, z2)
                        }

                        val faces = listOf(
                            listOf(0, 1, 2, 3), listOf(4, 5, 6, 7),
                            listOf(0, 4, 7, 3), listOf(1, 5, 6, 2),
                            listOf(0, 1, 5, 4), listOf(3, 2, 6, 7)
                        )

                        val sortedFaces = faces.mapIndexed { index, faceIndices ->
                            val avgZ = faceIndices.sumOf { projected[it].third.toDouble() } / 4.0
                            index to avgZ
                        }.sortedBy { it.second }

                        sortedFaces.forEach { (faceIdx, _) ->
                            val indices = faces[faceIdx]
                            val path = Path().apply {
                                moveTo(projected[indices[0]].first, projected[indices[0]].second)
                                lineTo(projected[indices[1]].first, projected[indices[1]].second)
                                lineTo(projected[indices[2]].first, projected[indices[2]].second)
                                lineTo(projected[indices[3]].first, projected[indices[3]].second)
                                close()
                            }
                            val tint = (faceIdx * 10f) / 100f
                            drawPath(path = path, color = primaryColor.copy(alpha = 0.9f + tint/2))
                            drawIntoCanvas { canvas ->
                                val text = faceNumbers[faceIdx].toString()
                                val paint = Paint().apply {
                                    color = onPrimaryColor.toArgb()
                                    textSize = 14 * density
                                    textAlign = Paint.Align.CENTER
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                val centerX = indices.sumOf { projected[it].first.toDouble() } / 4.0
                                val centerY = indices.sumOf { projected[it].second.toDouble() } / 4.0
                                val bounds = Rect()
                                paint.getTextBounds(text, 0, text.length, bounds)
                                val textY = centerY + (bounds.height() / 2)
                                canvas.nativeCanvas.drawText(text, centerX.toFloat(), textY.toFloat(), paint)
                            }
                        }
                    }
                } else if (rollOutcome != null) {
                    val scale by animateFloatAsState(
                        targetValue = 1.25f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "result"
                    )
                    Text(
                        text = "${rollOutcome!!.total}",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.scale(scale),
                        color = when {
                            rollOutcome!!.isCriticalSuccess -> Color(0xFF4CAF50)
                            rollOutcome!!.isCriticalFailure -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                } else {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRolling = true
                                delay(1200)
                                rollOutcome = performRoll(rollData, advantageMode)
                                isRolling = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Casino, null, modifier = Modifier.size(85.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (rollOutcome != null && !isRolling) {
                Spacer(Modifier.height(20.dp))
                
                Text(
                    text = buildBreakdownString(rollOutcome!!, rollData),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (rollOutcome!!.isCriticalSuccess) {
                    Text(labelCritSuccess, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                } else if (rollOutcome!!.isCriticalFailure) {
                    Text(labelCritFailure, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isRolling = true
                            delay(1000)
                            rollOutcome = performRoll(rollData, advantageMode)
                            isRolling = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(labelRollAgain)
                }
            } else if (!isRolling) {
                Spacer(Modifier.height(12.dp))
                Text(labelTapToRoll, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alpha(0.7f))
            }
        }
    }
}

@Composable
fun AdvantageButton(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

private fun performRoll(data: DiceRollData, mode: AdvantageMode): RollOutcome {
    val r1 = Random.nextInt(1, data.diceSides + 1)
    var r2: Int? = null
    
    val winningDieValue = if (data.diceSides == 20 && data.diceCount == 1) {
        when (mode) {
            AdvantageMode.Advantage -> {
                r2 = Random.nextInt(1, 21)
                maxOf(r1, r2)
            }
            AdvantageMode.Disadvantage -> {
                r2 = Random.nextInt(1, 21)
                minOf(r1, r2)
            }
            else -> r1
        }
    } else {
        var total = r1
        for (i in 1 until data.diceCount) {
            total += Random.nextInt(1, data.diceSides + 1)
        }
        total
    }

    val totalBonus = data.baseModifier + data.proficiencyBonus
    
    return RollOutcome(
        firstRoll = r1,
        secondRoll = r2,
        modifier = totalBonus,
        total = winningDieValue + totalBonus,
        isCriticalSuccess = data.diceSides == 20 && winningDieValue == 20,
        isCriticalFailure = data.diceSides == 20 && winningDieValue == 1,
        mode = mode
    )
}

private fun buildBreakdownString(outcome: RollOutcome, data: DiceRollData): String {
    val rollPart = if (outcome.secondRoll != null) {
        val modeStr = if (outcome.mode == AdvantageMode.Advantage) "Adv" else "Dis"
        "(${outcome.firstRoll} vs ${outcome.secondRoll}) [$modeStr]"
    } else {
        "(${outcome.firstRoll})"
    }
    
    val modPart = if (outcome.modifier > 0) " + ${outcome.modifier}" else if (outcome.modifier < 0) " - ${-outcome.modifier}" else ""
    return "$rollPart$modPart = ${outcome.total}"
}
