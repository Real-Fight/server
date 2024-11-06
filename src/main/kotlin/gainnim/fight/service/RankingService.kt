package gainnim.fight.service

import gainnim.fight.dto.response.RankingResponse
import gainnim.fight.entity.User
import gainnim.fight.repository.UserRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RankingService(val redisTemplate: RedisTemplate<String, Any>, val userRepository: UserRepository) {
    val zSetOperations = redisTemplate.opsForZSet()
//    val hashOperations = redisTemplate.opsForHash<String, String>()
    val rankingKey = "ranking"
    val userProfileKey = "profile"
    fun updateRanking(userId: UUID, gainedPower: Long) {
//        zSetOperations.add(rankingKey, userId, totalPower.toDouble()) // todo look
        zSetOperations.incrementScore(rankingKey, userId.toString(), gainedPower.toDouble())
    }
    fun getRankingFromUserId(userId: UUID): Long {
        return zSetOperations.reverseRank(rankingKey, userId.toString())!! + 1
    }
    fun getRanking(page: Long): List<RankingResponse> {
        val ranks = zSetOperations.reverseRange(rankingKey, page, page+99)
        return ranks!!.map {
            val userId = UUID.fromString(it.toString())
            val user = userRepository.findUserById(userId)!! // todo tlqkf optimization
            RankingResponse(
                    id = userId,
                    name = user.name,
                    profileImgUrl = user.profileImgUrl,
                    ranking = getRankingFromUserId(userId),
                    totalPower = zSetOperations.score(rankingKey, userId.toString())!!.toLong() // todo
            )
        }
    }
    fun setRanking(userId: UUID, totalPower: Long) {
        zSetOperations.add(rankingKey, userId.toString(), totalPower.toDouble())
    }
    fun initializeRanking(users: List<User>) {
        redisTemplate.delete(rankingKey)
        users.map {
            setRanking(it.id, it.totalPower)
//            zSetOperations.add(rankingKey, it.id, it.totalPower.toDouble())
//            updateRanking(it.id, it.totalPower)
        }
    }
}