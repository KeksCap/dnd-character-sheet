package com.example.dndhelper.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

// --- Particle system for crits ---
data class Particle(
    var x: Float, var y: Float,
    val vx: Float, val vy: Float,
    val color: Color, val size: Float,
    var life: Float = 1f,
    val decay: Float = 0.02f,
    val symbol: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollBottomSheet(
    rollData: DiceRollData,
    onDismiss: () -> Unit,
    onRollComplete: ((DiceRollData, RollOutcome) -> Unit)? = null
) {
    // --- TRANSLATIONS ---
    val labelDisadvantage = tr("Помеха", "Disadv.")
    val labelNormal = tr("Норма", "Normal")
    val labelAdvantage = tr("Преимущ.", "Advant.")
    val labelCritSuccess = tr("КРИТИЧЕСКИЙ УСПЕХ!", "CRITICAL SUCCESS!")
    val labelCritFailure = tr("КРИТИЧЕСКИЙ ПРОВАЛ!", "CRITICAL FAIL!")
    val labelRollAgain = tr("Бросить еще раз", "Roll Again")
    val labelTapToRoll = tr("Нажми на кубик", "Tap the die")
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var advantageMode by remember { mutableStateOf(AdvantageMode.None) }
    var rollOutcome by remember { mutableStateOf<RollOutcome?>(null) }
    var isRolling by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    
    // Particles
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    var particlesActive by remember { mutableStateOf(false) }

    // Haptic feedback
    val view = androidx.compose.ui.platform.LocalView.current
    fun doHaptic(heavy: Boolean = false) {
        try {
            if (heavy) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            } else {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } catch (_: Exception) {}
    }

    // Generate particles for critical results
    fun spawnParticles(isCrit: Boolean) {
        val colors = if (isCrit) {
            listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFFF00), Color(0xFF4CAF50))
        } else {
            listOf(Color(0xFFF44336), Color(0xFFFF5722), Color(0xFF9C27B0), Color(0xFF880000))
        }
        val symbols = if (isCrit) listOf("✦", "★", "✧", "⚡") else listOf("☠", "✖", "💀", "⚔")
        particles = List(30) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 4f + 2f
            Particle(
                x = 0f, y = 0f,
                vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
                vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f,
                decay = Random.nextFloat() * 0.015f + 0.01f,
                symbol = if (Random.nextFloat() > 0.6f) symbols.random() else null
            )
        }
        particlesActive = true
    }

    // Animate particles
    LaunchedEffect(particlesActive) {
        if (particlesActive) {
            while (particles.any { it.life > 0f }) {
                particles = particles.map {
                    it.copy(
                        x = it.x + it.vx,
                        y = it.y + it.vy,
                        life = (it.life - it.decay).coerceAtLeast(0f)
                    )
                }
                delay(16)
            }
            particlesActive = false
        }
    }

    // Cube rotation
    val infiniteTransition = rememberInfiniteTransition(label = "DiceRotation")
    val angleX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "X"
    )
    val angleY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "Y"
    )

    // Face numbers shuffling
    var faceNumbers by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6)) }
    LaunchedEffect(isRolling) {
        if (isRolling) {
            while (isRolling) {
                faceNumbers = List(6) { Random.nextInt(1, rollData.diceSides + 1) }
                delay(80) 
            }
        }
    }

    // Glow animation for crits
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowPulse"
    )

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
                        onClick = { advantageMode = AdvantageMode.Disadvantage; rollOutcome = null; showResult = false }
                    )
                    AdvantageButton(
                        label = labelNormal,
                        selected = advantageMode == AdvantageMode.None,
                        onClick = { advantageMode = AdvantageMode.None; rollOutcome = null; showResult = false }
                    )
                    AdvantageButton(
                        label = labelAdvantage,
                        selected = advantageMode == AdvantageMode.Advantage,
                        onClick = { advantageMode = AdvantageMode.Advantage; rollOutcome = null; showResult = false }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // Main dice area
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect behind result for crits
                if (showResult && rollOutcome != null && (rollOutcome!!.isCriticalSuccess || rollOutcome!!.isCriticalFailure)) {
                    val glowColor = if (rollOutcome!!.isCriticalSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .alpha(glowAlpha * 0.4f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(glowColor, glowColor.copy(alpha = 0f)),
                                    radius = 200f
                                )
                            )
                    )
                }

                // Circle background
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

                            // Gradient-like shading on cube faces
                            val faceColors = listOf(
                                primaryColor.copy(alpha = 0.7f),
                                primaryColor.copy(alpha = 0.85f),
                                primaryColor.copy(alpha = 0.75f),
                                primaryColor.copy(alpha = 0.9f),
                                primaryColor.copy(alpha = 0.65f),
                                primaryColor.copy(alpha = 1.0f)
                            )

                            sortedFaces.forEach { (faceIdx, avgZ) ->
                                val indices = faces[faceIdx]
                                val path = Path().apply {
                                    moveTo(projected[indices[0]].first, projected[indices[0]].second)
                                    lineTo(projected[indices[1]].first, projected[indices[1]].second)
                                    lineTo(projected[indices[2]].first, projected[indices[2]].second)
                                    lineTo(projected[indices[3]].first, projected[indices[3]].second)
                                    close()
                                }
                                // Depth-based shading
                                val depthFactor = ((avgZ + halfSize) / (halfSize * 2)).toFloat().coerceIn(0.4f, 1.0f)
                                drawPath(path = path, color = faceColors[faceIdx].copy(alpha = depthFactor))

                                // Edge highlight
                                drawPath(path = path, color = Color.White.copy(alpha = 0.15f), 
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))

                                drawIntoCanvas { canvas ->
                                    val text = faceNumbers[faceIdx].toString()
                                    val paint = Paint().apply {
                                        color = onPrimaryColor.toArgb()
                                        textSize = 14 * density
                                        textAlign = Paint.Align.CENTER
                                        isFakeBoldText = true
                                        isAntiAlias = true
                                        setShadowLayer(3f * density, 0f, 1f * density, android.graphics.Color.argb(80, 0, 0, 0))
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
                    } else if (showResult && rollOutcome != null) {
                        val scale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "resultBounce"
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
                                    showResult = false
                                    isRolling = true
                                    delay(1200)
                                    rollOutcome = performRoll(rollData, advantageMode)
                                    isRolling = false
                                    doHaptic(false)
                                    showResult = true
                                    onRollComplete?.invoke(rollData, rollOutcome!!)
                                    if (rollOutcome!!.isCriticalSuccess || rollOutcome!!.isCriticalFailure) {
                                        doHaptic(true)
                                        spawnParticles(rollOutcome!!.isCriticalSuccess)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Casino, null, modifier = Modifier.size(85.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Particle overlay
                if (particlesActive && particles.isNotEmpty()) {
                    val density = LocalDensity.current.density
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        particles.filter { it.life > 0f }.forEach { p ->
                            val px = cx + p.x * density * 3f
                            val py = cy + p.y * density * 3f
                            if (p.symbol != null) {
                                drawIntoCanvas { canvas ->
                                    val paint = Paint().apply {
                                        color = p.color.copy(alpha = p.life).toArgb()
                                        textSize = p.size * density * 0.7f
                                        textAlign = Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    canvas.nativeCanvas.drawText(p.symbol, px, py, paint)
                                }
                            } else {
                                drawCircle(
                                    color = p.color.copy(alpha = p.life),
                                    radius = p.size * (0.5f + p.life * 0.5f),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }
            }

            if (showResult && rollOutcome != null && !isRolling) {
                Spacer(Modifier.height(20.dp))
                
                Text(
                    text = buildBreakdownString(rollOutcome!!, rollData),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (rollOutcome!!.isCriticalSuccess) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        labelCritSuccess, 
                        color = Color(0xFF4CAF50), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.alpha(glowAlpha)
                    )
                } else if (rollOutcome!!.isCriticalFailure) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        labelCritFailure, 
                        color = Color(0xFFF44336), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.alpha(glowAlpha)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            showResult = false
                            particles = emptyList()
                            particlesActive = false
                            isRolling = true
                            delay(1000)
                            rollOutcome = performRoll(rollData, advantageMode)
                            isRolling = false
                            doHaptic(false)
                            showResult = true
                            onRollComplete?.invoke(rollData, rollOutcome!!)
                            if (rollOutcome!!.isCriticalSuccess || rollOutcome!!.isCriticalFailure) {
                                doHaptic(true)
                                spawnParticles(rollOutcome!!.isCriticalSuccess)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(labelRollAgain)
                }
            } else if (!isRolling && !showResult) {
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
