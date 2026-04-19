package com.example.dndhelper.utils

import com.example.dndhelper.data.GameLogEntry
import com.example.dndhelper.data.LogType

/**
 * Менеджер игрового журнала.
 * Все методы возвращают GameLogEntry, который UI-слой добавляет в хранилище.
 */
object GameLogManager {

    const val MAX_LOG_SIZE = 200

    // --- УРОН ---
    fun logDamage(amount: Int, currentHp: Int, source: String = ""): GameLogEntry {
        val srcRu = if (source.isNotEmpty()) " ($source)" else ""
        val srcEn = if (source.isNotEmpty()) " ($source)" else ""
        return GameLogEntry(
            type = LogType.DAMAGE,
            messageRu = "Получено $amount урона$srcRu. HP: $currentHp",
            messageEn = "Took $amount damage$srcEn. HP: $currentHp"
        )
    }

    // --- ЛЕЧЕНИЕ ---
    fun logHeal(amount: Int, currentHp: Int, source: String = ""): GameLogEntry {
        val srcRu = if (source.isNotEmpty()) " ($source)" else ""
        val srcEn = if (source.isNotEmpty()) " ($source)" else ""
        return GameLogEntry(
            type = LogType.HEAL,
            messageRu = "Восстановлено $amount HP$srcRu. HP: $currentHp",
            messageEn = "Healed $amount HP$srcEn. HP: $currentHp"
        )
    }

    // --- ЗЕЛЬЕ ---
    fun logPotion(potionNameRu: String, potionNameEn: String, healAmount: Int, currentHp: Int): GameLogEntry {
        return GameLogEntry(
            type = LogType.POTION,
            messageRu = "Выпито $potionNameRu. Восстановлено $healAmount HP. HP: $currentHp",
            messageEn = "Drank $potionNameEn. Healed $healAmount HP. HP: $currentHp"
        )
    }

    // --- ЯЧЕЙКА ЗАКЛИНАНИЯ ---
    fun logSpellSlotUsed(level: Int, remaining: Int, max: Int): GameLogEntry {
        return GameLogEntry(
            type = LogType.SPELL_SLOT,
            messageRu = "Использована ячейка $level-го уровня. Осталось: $remaining/$max",
            messageEn = "Used level $level spell slot. Remaining: $remaining/$max"
        )
    }

    fun logSpellSlotRestored(level: Int, remaining: Int, max: Int): GameLogEntry {
        return GameLogEntry(
            type = LogType.SPELL_SLOT,
            messageRu = "Восстановлена ячейка $level-го уровня. Осталось: $remaining/$max",
            messageEn = "Restored level $level spell slot. Remaining: $remaining/$max"
        )
    }

    // --- ОТДЫХ ---
    fun logShortRest(hitDiceType: Int, diceRoll: Int, conMod: Int, healAmount: Int): GameLogEntry {
        return GameLogEntry(
            type = LogType.REST,
            messageRu = "☕ Короткий отдых. Бросок d$hitDiceType: $diceRoll + КОН($conMod) = $healAmount HP",
            messageEn = "☕ Short Rest. Roll d$hitDiceType: $diceRoll + CON($conMod) = $healAmount HP"
        )
    }

    fun logLongRest(restoredHp: Int, diceRestored: Int): GameLogEntry {
        return GameLogEntry(
            type = LogType.REST,
            messageRu = "🌙 Длительный отдых. HP полностью восстановлены ($restoredHp). Ячейки восстановлены. Кости хитов: +$diceRestored",
            messageEn = "🌙 Long Rest. HP fully restored ($restoredHp). Spell slots restored. Hit dice: +$diceRestored"
        )
    }

    // --- СОСТОЯНИЕ ---
    fun logConditionAdded(conditionRu: String, conditionEn: String): GameLogEntry {
        return GameLogEntry(
            type = LogType.CONDITION,
            messageRu = "⚡ Получено состояние: $conditionRu",
            messageEn = "⚡ Condition applied: $conditionEn"
        )
    }

    fun logConditionRemoved(conditionRu: String, conditionEn: String): GameLogEntry {
        return GameLogEntry(
            type = LogType.CONDITION,
            messageRu = "✓ Снято состояние: $conditionRu",
            messageEn = "✓ Condition removed: $conditionEn"
        )
    }

    fun logExhaustionChanged(oldLevel: Int, newLevel: Int): GameLogEntry {
        val direction = if (newLevel > oldLevel) "↑" else "↓"
        return GameLogEntry(
            type = LogType.CONDITION,
            messageRu = "$direction Истощение: $oldLevel → $newLevel",
            messageEn = "$direction Exhaustion: $oldLevel → $newLevel"
        )
    }

    // --- БРОСОК КУБИКА ---
    fun logDiceRoll(titleRu: String, titleEn: String, detailsRu: String, detailsEn: String): GameLogEntry {
        return GameLogEntry(
            type = LogType.DICE_ROLL,
            messageRu = "🎲 $titleRu: $detailsRu",
            messageEn = "🎲 $titleEn: $detailsEn"
        )
    }

    // --- ЗАМЕТКА ---
    fun createNote(text: String): GameLogEntry {
        return GameLogEntry(
            type = LogType.NOTE,
            messageRu = text,
            messageEn = text
        )
    }
}
