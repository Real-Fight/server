package gainnim.fight.service

import gainnim.fight.dto.response.HistoryResponse
import gainnim.fight.repository.GameHistoryRepository
import gainnim.fight.repository.UserRepository
import gainnim.fight.util.error.CustomError
import gainnim.fight.util.error.ErrorState
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GameHistoryService(val gameHistoryRepository: GameHistoryRepository, val userRepository: UserRepository) {
    fun getHistory(userId: UUID): List<HistoryResponse> {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        return gameHistoryRepository.findGameHistoriesByUserOrderByCreatedAtDesc(user).map {
            HistoryResponse(
                    matchType = it.matchType,
                    result = it.result,
                    score = it.score
            )
        }
    }
}