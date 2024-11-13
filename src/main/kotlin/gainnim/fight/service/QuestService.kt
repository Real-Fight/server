package gainnim.fight.service

import gainnim.fight.dto.response.QuestResponse
import gainnim.fight.entity.Quest
import gainnim.fight.entity.QuestType
import gainnim.fight.handler.MatchType
import gainnim.fight.repository.GameHistoryRepository
import gainnim.fight.repository.QuestRepository
import gainnim.fight.repository.UserRepository
import gainnim.fight.util.error.CustomError
import gainnim.fight.util.error.ErrorState
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class QuestService(val userRepository: UserRepository, val questRepository: QuestRepository, val gameHistoryRepository: GameHistoryRepository) {
    val random = Random()
    lateinit var startOfDay: Date
    lateinit var endOfDay: Date
    fun getQuest(userId: UUID): QuestResponse {  // todo we should move it to websocketHandler, it is must run at endGame // optimization
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        val quest = questRepository.findQuestByUser(user) ?: questRepository.save(
                Quest(
                        user = user,
                        level = user.totalPower / 50 + 1,
                        type = QuestType.values()[random.nextInt(3)],
                )
        )
        val goal = quest.level * quest.type.length
        if (quest.count >= goal) {
            if (quest.completed == false) {
                val gainedWillPower = (2 * 0.01 * 100 * (300 / (user.willpower + 10))).toLong()
                userRepository.save(user.copy(willpower = user.willpower + gainedWillPower))
                questRepository.save(quest.copy(completed = true))
            }
            return QuestResponse(
                    message = quest.type.message,
                    questType = quest.type,
                    completed = true
            )
        }
        val count: Int = when (quest.type) {
            QuestType.SQUAT -> {
                val matchTypes = listOf<MatchType>(
                        MatchType.SHORTSQUAT,
                        MatchType.MIDDLESQUAT,
                        MatchType.LONGPUSHUP
                )
                val gameHistories = gameHistoryRepository.findGameHistoriesByUserAndMatchTypeInAndCreatedAtBetween(user, matchTypes, startOfDay, endOfDay)
                gameHistories.sumOf { it.score }
            }
            QuestType.PUSHUP -> {
                val matchTypes = listOf<MatchType>(
                        MatchType.SHORTPUSHUP,
                        MatchType.MIDDLEPUSHUP,
                        MatchType.LONGPUSHUP
                )
                val gameHistories = gameHistoryRepository.findGameHistoriesByUserAndMatchTypeInAndCreatedAtBetween(user, matchTypes, startOfDay, endOfDay)
                gameHistories.sumOf { it.score }
            }
            QuestType.MATCH -> {
                gameHistoryRepository.countGameHistoriesByUserAndCreatedAtBetween(user, startOfDay, endOfDay)
            }
        }
        return QuestResponse(
                message = quest.type.message + "(${count}/${goal})",
                questType = quest.type,
                completed = false
        )
    }
    @PostConstruct
    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    fun initQuest() {
        questRepository.deleteAll()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startOfDay = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        endOfDay = calendar.time
    }
}