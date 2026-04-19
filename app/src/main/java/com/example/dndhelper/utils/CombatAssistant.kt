package com.example.dndhelper.utils

import androidx.compose.ui.graphics.Color
import com.example.dndhelper.data.CharacterSaveData

enum class AdviceCategory {
    CRITICAL, TACTICAL, REMINDER
}

data class OracleAdvice(
    val category: AdviceCategory,
    val titleRu: String,
    val titleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val isEn: Boolean = false
) {
    val title: String get() = if (isEn) titleEn else titleRu
    val description: String get() = if (isEn) descriptionEn else descriptionRu
}

object CombatAssistant {

    fun getAdvice(character: CharacterSaveData, isEn: Boolean): List<OracleAdvice> {
        val adviceList = mutableListOf<OracleAdvice>()

        // 1. Порог Здоровья (HP Threshold)
        val hpPercentage = if (character.maxHp > 0) character.currentHp.toFloat() / character.maxHp else 0f
        if (hpPercentage <= 0.25f && character.currentHp > 0) {
            adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.CRITICAL,
                    titleRu = "Низкое здоровье",
                    titleEn = "Low Health",
                    descriptionRu = "Внимание! У вас критически мало очков здоровья. Рассмотрите возможность использования действия 'Отход' (Disengage), чтобы избежать провоцированных атак, или запросите лечение у союзников.",
                    descriptionEn = "Warning! Your health points are critically low. Consider using the 'Disengage' action to avoid opportunity attacks, or request healing from allies.",
                    isEn = isEn
                )
            )
        } else if (character.currentHp == 0) {
             adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.CRITICAL,
                    titleRu = "Без сознания",
                    titleEn = "Unconscious",
                    descriptionRu = "Вы находитесь без сознания. Ваш союзник должен стабилизировать вас или вылечить, иначе вам придется совершать спасброски от смерти.",
                    descriptionEn = "You are unconscious. An ally must stabilize or heal you, otherwise you will have to make death saving throws.",
                    isEn = isEn
                )
            )
        }

        // 2. Истощение (Exhaustion)
        if (character.use2024Rules && character.exhaustionLevel > 0) {
            adviceList.add(
                 OracleAdvice(
                    category = AdviceCategory.REMINDER,
                    titleRu = "Истощение",
                    titleEn = "Exhaustion",
                    descriptionRu = "У вас ${character.exhaustionLevel} уровень истощения. Вы получаете штраф -${character.exhaustionLevel * 2} ко всем броскам д20 (атака, спасброски, проверки характеристик) и СЛ ваших заклинаний снижена на ${character.exhaustionLevel * 2}.",
                    descriptionEn = "You have exhaustion level ${character.exhaustionLevel}. You take a -${character.exhaustionLevel * 2} penalty to all d20 rolls (attacks, saving throws, ability checks) and your spell save DC is reduced by ${character.exhaustionLevel * 2}.",
                    isEn = isEn
                )
            )
        } else if (!character.use2024Rules && character.exhaustionLevel > 0) {
            val effRu = when (character.exhaustionLevel) {
                1 -> "Помеха на проверки характеристик."
                2 -> "Скорость уменьшена вдвое."
                3 -> "Помеха на броски атаки и спасброски."
                4 -> "Максимум хитов уменьшен вдвое."
                5 -> "Скорость равна 0."
                else -> "Смерть."
            }
             val effEn = when (character.exhaustionLevel) {
                1 -> "Disadvantage on ability checks."
                2 -> "Speed halved."
                3 -> "Disadvantage on attack rolls and saving throws."
                4 -> "Hit point maximum halved."
                5 -> "Speed reduced to 0."
                else -> "Death."
            }
            adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.REMINDER,
                    titleRu = "Истощение",
                    titleEn = "Exhaustion",
                    descriptionRu = "У вас ${character.exhaustionLevel} уровень истощения. Текущий эффект: $effRu",
                    descriptionEn = "You have exhaustion level ${character.exhaustionLevel}. Current effect: $effEn",
                    isEn = isEn
                )
            )
        }

        // 3. Анализ состояний (Conditions)
        val conds = character.activeConditions
        
        if (conds.any { it.contains("Опутан") || it.contains("Restrained") }) {
            adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Вы Опутаны",
                    titleEn = "You are Restrained",
                    descriptionRu = "Ваша скорость равна 0. Атаки по вам совершаются с ПРЕИМУЩЕСТВОМ, а ваши атаки — с ПОМЕХОЙ. Кроме того, у вас помеха на спасброски Ловкости.",
                    descriptionEn = "Your speed is 0. Attacks against you have ADVANTAGE, and your attacks have DISADVANTAGE. You also have disadvantage on Dexterity saving throws.",
                    isEn = isEn
                )
            )
        }

        if (conds.any { it.contains("Схвачен") || it.contains("Grappled") }) {
             adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Вы Схвачены",
                    titleEn = "You are Grappled",
                    descriptionRu = "Ваша скорость равна 0. Вы можете использовать действие для попытки вырваться (проверка Силы/Атлетики или Ловкости/Акробатики против захватчика).",
                    descriptionEn = "Your speed is 0. You can use your action to attempt to escape (Strength/Athletics or Dexterity/Acrobatics check contested by the grappler).",
                    isEn = isEn
                )
            )
        }

        if (conds.any { it.contains("Ослеплен") || it.contains("Blinded") }) {
             adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Слепота",
                    titleEn = "Blindness",
                    descriptionRu = "Вы автоматически проваливаете любые проверки, требующие зрения. В бою ваши броски атаки имеют ПОМЕХУ, а враги бьют вас с ПРЕИМУЩЕСТВОМ.",
                    descriptionEn = "You automatically fail any checks requiring sight. In combat, your attack rolls have DISADVANTAGE, and enemies hit you with ADVANTAGE.",
                    isEn = isEn
                )
            )
        }

        if (conds.any { it.contains("Отравлен") || it.contains("Poisoned") }) {
            adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Отравлен",
                    titleEn = "Poisoned",
                    descriptionRu = "Пока вы отравлены, вы совершаете броски атаки и проверки характеристик с ПОМЕХОЙ.",
                    descriptionEn = "While poisoned, you make attack rolls and ability checks with DISADVANTAGE.",
                    isEn = isEn
                )
            )
        }

        if (conds.any { it.contains("Сбит с ног") || it.contains("Повален") || it.contains("Prone") }) {
            adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Лежачее положение",
                    titleEn = "Prone Position",
                    descriptionRu = "Вы повалены! Чтобы встать, вам нужно потратить половину своей скорости. Атаки ближнего боя по вам (в пределах 5 футов) совершаются с ПРЕИМУЩЕСТВОМ.",
                    descriptionEn = "You are prone! Standing up costs half your movement speed. Melee attacks against you (within 5 feet) have ADVANTAGE.",
                    isEn = isEn
                )
            )
        }

        // Если все хорошо и нет специфичных тактик
        if (adviceList.isEmpty()) {
             adviceList.add(
                OracleAdvice(
                    category = AdviceCategory.TACTICAL,
                    titleRu = "Готовность к бою",
                    titleEn = "Ready for Combat",
                    descriptionRu = "Персонаж здоров и не имеет мешающих состояний. Ищите тактическое преимущество на поле боя: используйте укрытия и не забывайте про бонусные действия.",
                    descriptionEn = "The character is healthy and has no hindering conditions. Look for a tactical advantage on the battlefield: use cover and don't forget about bonus actions.",
                    isEn = isEn
                )
            )
        } else {
             // Сортировка советов: CRITICAL -> TACTICAL -> REMINDER
             adviceList.sortBy { it.category }
        }

        return adviceList
    }

    fun getCategoryColor(category: AdviceCategory): Color {
        return when (category) {
            AdviceCategory.CRITICAL -> Color(0xFFE57373) // Яркий красный/коралловый
            AdviceCategory.TACTICAL -> Color(0xFF64B5F6) // Синий
            AdviceCategory.REMINDER -> Color(0xFFFFD54F) // Желтый/Оранжевый
        }
    }
}
