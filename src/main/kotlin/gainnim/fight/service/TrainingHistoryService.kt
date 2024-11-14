package gainnim.fight.service

import gainnim.fight.dto.request.TrainingHistoryRequest
import gainnim.fight.dto.response.TrainingHistoryResponse
import gainnim.fight.entity.TrainingHistory
import gainnim.fight.entity.TrainingType
import gainnim.fight.repository.GameHistoryRepository
import gainnim.fight.repository.TrainingHistoryRepository
import gainnim.fight.repository.UserRepository
import gainnim.fight.util.error.CustomError
import gainnim.fight.util.error.ErrorState
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TrainingHistoryService(val userRepository: UserRepository, val trainingHistoryRepository: TrainingHistoryRepository) {
    fun getMyTrainingHistory(userId: UUID, trainingType: TrainingType): List<TrainingHistoryResponse> {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        val trainingHistories = trainingHistoryRepository.findTrainingHistoriesByUserAndTrainingTypeOrderByCreatedAtDesc(user, trainingType)
        return trainingHistories.map {
            TrainingHistoryResponse(
                    trainingType = it.trainingType,
                    count = it.count,
                    createdAt = it.createdAt
            )
        }
    }

    fun postTrainingHistory(userId: UUID, request: TrainingHistoryRequest) {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        trainingHistoryRepository.save(TrainingHistory(
                user = user,
                trainingType = request.trainingType,
                count = request.count
        ))
    }
}