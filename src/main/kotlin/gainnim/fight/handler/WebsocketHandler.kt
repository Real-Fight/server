package gainnim.fight.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import gainnim.fight.entity.GameHistory
import gainnim.fight.entity.Result
import gainnim.fight.repository.GameHistoryRepository
import gainnim.fight.repository.UserRepository
import gainnim.fight.service.RankingService
import gainnim.fight.util.jwt.JwtProvider
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.sql.Timestamp
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

@Component
class WebsocketHandler(val jwtProvider: JwtProvider, val rankingService: RankingService, val userRepository: UserRepository, val gameHistoryRepository: GameHistoryRepository): TextWebSocketHandler() {
    val log = LoggerFactory.getLogger(javaClass)
    val sessions: MutableMap<UUID, WebSocketSession> = mutableMapOf() // user -> id, session
    val userIds: MutableMap<WebSocketSession, UUID> = mutableMapOf() // user -> session, id
    val matchQueue: MutableMap<String, Queue<UUID>> = mutableMapOf(
            "SHORTSQUAT" to LinkedList(),
            "MIDDLESQUAT" to LinkedList(),
            "LONGSQUAT" to LinkedList(),
            "SHORTPUSHUP" to LinkedList(),
            "MIDDLEPUSHUP" to LinkedList(),
            "LONGPUSHUP" to LinkedList(),
            "SHORTSITUP" to LinkedList(),
            "MIDDLESITUP" to LinkedList(),
            "LONGSITUP" to LinkedList()
    )
    val rooms: MutableMap<UUID, Room> = mutableMapOf()
    val inGameUserSessionsAndRoomIds: MutableMap<WebSocketSession, UUID> = mutableMapOf()  // session, roomId
    val endTimestamps: MutableMap<UUID, Timestamp> = mutableMapOf() // roomId, endTimestamp
    val objectMapper = jacksonObjectMapper()
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = objectMapper.readValue<Message>(message.payload)  // todo look lower
            log.info(message.payload)
            log.info(payload.event.name)
            when (payload.event.name) {
                "RANDOMMATCH" -> randomMatch(session, payload.data["token"] as String, MatchType.valueOf(payload.data["matchType"] as String))
                "MATCHINGCANCEL" -> matchingCancel(session)
                "READIED" -> readied(session, UUID.fromString(payload.data["roomId"] as String))
                "READYINGCANCEL" -> readyingCancel(session, UUID.fromString(payload.data["roomId"] as String))
                "SCOREUPDATE" -> scoreUpdate(session, ScoreUpdateData.fromMap(payload.data))
                "GIVEUP" -> giveup(session)
            }
        } catch (e: Exception) {
            sendMessage(session, Event.BADREQUEST, mapOf())
        }
    }
    fun randomMatch(session: WebSocketSession, token: String, matchType: MatchType) {
        log.info(matchType.name)
        try {
            val userId = jwtProvider.getUuidByToken(token)
            sessions[userId]?.let { throw Exception() } // todo look
            val queue = matchQueue[matchType.name] ?: throw Exception()
            sessions.put(userId, session)
            userIds.put(session, userId)
            if (queue.isEmpty()) {
                queue.add(userId)
                sendMessage(session, Event.MATCHOK, mapOf())
            } else {
                val matchedUserId = queue.remove()
                val roomId = UUID.randomUUID()
                rooms.put(roomId, Room(
                        matchType = matchType,
                        scores = mutableMapOf(
                                userId to 0,
                                matchedUserId to 0
                        ),
                        users = mutableMapOf(
                                userId to UserStatus.READY,
                                matchedUserId to UserStatus.READY
                        )
                ))
                val user = userRepository.findUserById(userId)!!  //todo Optimization and remove !!
                val matchedUser = userRepository.findUserById(matchedUserId)!!  //todo Optimization and remove !!
                val userRanking = rankingService.getRankingFromUserId(userId)
                val matchedUserRanking = rankingService.getRankingFromUserId(matchedUserId)
                sendMessage(session, Event.MATCHED, MatchedData(
                        rivalId = matchedUserId,
                        rivalProfileImgUrl = matchedUser.profileImgUrl,
                        rivalName = matchedUser.name,
                        rivalRanking = matchedUserRanking,
                        userId = userId,
                        userProfileImgUrl = user.profileImgUrl,
                        userName = user.name,
                        userRanking = userRanking,
                        roomId = roomId
                ).toMap())
                sendMessage(sessions[matchedUserId]!!, Event.MATCHED, MatchedData(
                        rivalId = userId,
                        rivalProfileImgUrl = user.profileImgUrl,
                        rivalName = user.name,
                        rivalRanking = userRanking,
                        userId = matchedUserId,
                        userProfileImgUrl = matchedUser.profileImgUrl,
                        userName = matchedUser.name,
                        userRanking = matchedUserRanking,
                        roomId = roomId
                ).toMap())
            }
        } catch (e: Exception) {
            sendMessage(session, Event.FAILDMATCH, mapOf())
        }

    }
    fun matchingCancel(session: WebSocketSession) {
        val userId = userIds[session]!! // todo remove !! and try catch
        for (queue in matchQueue.values) {
            queue.remove(userId)
        }
        sessions.remove(userId)
        userIds.remove(session)
        sendMessage(session, Event.MATCHINGCANCELED, mapOf())
    }
    fun readied(session: WebSocketSession, roomId: UUID) {
        try {
            val userId = userIds[session]!! // todo remove !!
            val room = rooms[roomId]!! // todo remove !!
            if (!room.scores.containsKey(userId)) throw Exception()
            val rivals = room.users.filter { it.key != userId } // todo optimization
            if (rivals.containsValue(UserStatus.READY)) { // todo test, todo todo todo
                room.users[userId] = UserStatus.ON
            } else {
                room.users[userId] = UserStatus.ON
                room.status = Status.PROGRESS
                val endTimestamp = Timestamp(System.currentTimeMillis() + room.matchType.length)
                room.endTimestamp = endTimestamp
                endTimestamps.put(roomId, endTimestamp)
                val messageData = mapOf(
                        "room" to room
                )
                inGameUserSessionsAndRoomIds.put(session, roomId)
                sendMessage(session, Event.STARTGAME, messageData)
                rivals.keys.map {
                    val session = sessions[it]!!
                    inGameUserSessionsAndRoomIds.put(session, roomId)
                    sendMessage(session, Event.STARTGAME, messageData)
                }
            }
        } catch (e: Exception) {
            sendMessage(session, Event.FAILDREADY, mapOf())
        }
    }
    fun readyingCancel(session: WebSocketSession, roomId: UUID) {
        val userId = userIds[session]!!
        val room = rooms[roomId]!!
        val rivals = room.users.filter { it.key != userId }
        userIds.remove(session)
        sessions.remove(userId)
        sendMessage(session, Event.READYINGCANCELED, mapOf())
        rivals.keys.map {
            val session = sessions[it]!!
            sendMessage(session, Event.READYINGCANCELED, mapOf())
            userIds.remove(session)
            sessions.remove(it)
        }
        rooms.remove(roomId)
    }
    fun scoreUpdate(session: WebSocketSession, data: ScoreUpdateData) { // todo optimization
        try {
            val userId = userIds[session]!! // todo
            val room = rooms[data.roomId]!!
            if (room.status == Status.READY) throw Exception()
            room.scores[userId] = data.score
            room.users.filter { it.key != userId }.keys.map { sendMessage(sessions[it]!!, Event.SCOREUPDATE, data.toMap()) } // todo remove !!
        } catch (e: Exception) {
            sendMessage(session, Event.FAILDSCOREUPDATE, mapOf())
        }
    }
    fun giveup(session: WebSocketSession) {
        val roomId = inGameUserSessionsAndRoomIds[session]!!
        val room = rooms[roomId]!!
        val gainedStats = saveData(room, false)
        val userId = userIds[session]!!
        val winner = room.scores.filter { it.key != userId }
        val loser = room.scores.filter { it.key == userId }
        room.users.keys.map {
            val session = sessions[it]!!
            sendMessage(session, Event.ENDGAME,
                    mapOf(
                            "winner" to winner, // todo how to
                            "loser" to loser,
                            "draw" to false,
                            "gainedStats" to gainedStats[it]!!
                    )
            )
            userIds.remove(session)
            sessions.remove(it)
            inGameUserSessionsAndRoomIds.remove(session)
        }
        endTimestamps.remove(roomId)
        rooms.remove(roomId)
    }
    @Scheduled(fixedRate = 100)
    fun checkRoomEndTimestamp() {
        endTimestamps.map {
            if (it.value < Timestamp(System.currentTimeMillis())) {
                log.info("checkRoomEndTimestamp")
                val room = rooms[it.key]!!  // todo repect it
                val winner = room.scores.maxBy { it.value }
                val loser = room.scores.minBy { it.value }  // todo optimization
                var draw: Boolean = false
                if (winner.value == loser.value) {
                    draw = true
                }
                val gainedStats = saveData(room, draw)
                room.users.keys.map {
                    try {
                        val session = sessions[it]!!  // todo remove !! what is it
                        sendMessage(session, Event.ENDGAME,  // todo remove !!
                                mapOf(
                                        "winner" to winner,
                                        "loser" to loser,
                                        "draw" to draw,
                                        "gainedStats" to gainedStats[it]!!
                                )
                        )
                        userIds.remove(session)
                        sessions.remove(it)
                        inGameUserSessionsAndRoomIds.remove(session)
                    }catch (e: Exception) { }
                }
                endTimestamps.remove(it.key)
                rooms.remove(it.key)
            }
        }
    }
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) { // todo
        try {
            val userId = userIds[session]!! // todo remove !!
            for (queue in matchQueue.values) {
                queue.remove(userId)
            }
            sessions.remove(userId)
            userIds.remove(session)
            val roomId = inGameUserSessionsAndRoomIds[session]!!
            val room = rooms[roomId]!!
            val gainedStats = saveData(room, false)
            val winner = room.scores.filter { it.key != userId }
            val loser = room.scores.filter { it.key == userId }
            room.users.remove(userId)
            room.users.keys.map {
                val session = sessions[it]!!
                sendMessage(session, Event.ENDGAME,
                        mapOf(
                            "winner" to winner,
                            "loser" to loser,
                            "draw" to false,
                            "gainedStats" to gainedStats[it]!!
                        )
                )
                userIds.remove(session)
                sessions.remove(it)
                inGameUserSessionsAndRoomIds.remove(session)
            }
            endTimestamps.remove(roomId)
            rooms.remove(roomId)
        } catch (e: Exception) { }
    }
    fun saveData(room: Room, draw: Boolean): Map<UUID, Map<String, Long>> {  //todo
        val gainedStats = mutableMapOf<UUID, Map<String, Long>>()
        room.scores.map {
            val user = userRepository.findUserById(it.key) ?: throw Exception()  // todo jwt
            val gainedStrength = (room.matchType.strength * 0.01 * it.value * (300 / (user.strength + 10))).toLong()
            val gainedEndurance = (room.matchType.endurance * 0.01 * it.value * (300 / (user.endurance + 10 ))).toLong()
            val gainedAgility = (room.matchType.agility * 0.01 * it.value * (300 / (user.endurance + 10 ))).toLong()
            val gainedPower = gainedStrength + gainedEndurance + gainedAgility
            gainedStats[it.key] = mapOf(
                    "gainedStrength" to gainedStrength,
                    "gainedEndurance" to gainedEndurance,
                    "gainedAgility" to gainedAgility,
                    "gainedPower" to gainedPower
            )
            userRepository.save(
                    user.copy(
                            strength = user.strength + gainedStrength,
                            endurance = user.endurance + gainedEndurance,
                            agility = user.agility + gainedAgility,
                            totalPower = user.totalPower + gainedPower
                    )
            )
            val result = if (draw) Result.DRAW else if (it.value == room.scores.maxBy { it.value }?.value) Result.WIN else Result.LOSE
            gameHistoryRepository.save(
                    GameHistory(
                            user = user,
                            matchType = room.matchType,
                            result = result,
                            score = it.value
                    )
            )
            rankingService.updateRanking(it.key, gainedPower)
            }
        return gainedStats
//        }catch (e: Exception) {}
    }
    fun sendMessage(session: WebSocketSession, event: Event, data: Map<String, Any>) {
        val message = Message(
                event = event,
                data = data
        )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }
}
data class Message(
        val event: Event,
        val data: Map<String, Any>
)
data class MatchedData(
        val rivalId: UUID,
        val rivalProfileImgUrl: String,
        val rivalName: String,
        val rivalRanking: Long,
        val userId: UUID,
        val userProfileImgUrl: String,
        val userName: String,
        val userRanking: Long,
        val roomId: UUID
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
                "rivalId" to rivalId,
                "rivalProfileImgUrl" to rivalProfileImgUrl,
                "rivalName" to rivalName,
                "rivalRanking" to rivalRanking,
                "userId" to userId,
                "userProfileImgUrl" to userProfileImgUrl,
                "userName" to userName,
                "userRanking" to userRanking,
                "roomId" to roomId
        )
    }
}
data class Room(
        val matchType: MatchType,
        var status: Status = Status.READY,
        var endTimestamp: Timestamp? = null,
        val scores: MutableMap<UUID, Int>,  // id, score
        val users: MutableMap<UUID, UserStatus> // id
)
data class ScoreUpdateData(
        val roomId: UUID,
        val timestamp: Timestamp,
        val score: Int,
        val scoreChangeVolume: Int,
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
                "roomId" to roomId,
                "timestamp" to timestamp,
                "score" to score,
                "scoreChangeVolume" to scoreChangeVolume,
        )
    }
    companion object {
        fun fromMap(data: Map<String, Any>): ScoreUpdateData {
            return ScoreUpdateData(
                    roomId = UUID.fromString(data["roomId"] as String),
                    timestamp = Timestamp(data["timestamp"] as Long),
                    score = data["score"] as Int,
                    scoreChangeVolume = data["scoreChangeVolume"] as Int,
            )
        }
    }
}
enum class MatchType(val length: Int, val strength: Float, val endurance: Float, val agility: Float) {
    SHORTSQUAT(30000, 1f, 0.7f, 0.3f),
    MIDDLESQUAT(180000, 1f, 0.7f, 0.3f),
    LONGSQUAT(600000, 1f, 0.7f, 0.3f),
    SHORTPUSHUP(30000, 0.8f, 0.4f, 0.8f),
    MIDDLEPUSHUP(180000, 0.8f, 0.4f, 0.8f),
    LONGPUSHUP(600000, 0.8f, 0.4f, 0.8f),
    SHORTSITUP(30000, 0.4f, 0.8f, 0.8f),
    MIDDLESITUP(180000, 0.4f, 0.8f, 0.8f),
    LONGSITUP(600000, 0.4f, 0.8f, 0.8f);
    companion object {
        fun fromOrdinal(index: Int): MatchType {
            return values()[index]
        }
    }
}
enum class Status {
    READY, PROGRESS
}
enum class UserStatus {
    READY, ON
}
enum class Event {
    BADREQUEST,
    RANDOMMATCH,
    MATCHINGCANCEL,
    MATCHINGCANCELED,
    MATCHOK,
    FAILDMATCH,
    MATCHED,
    READIED,
    READYINGCANCEL,
    READYINGCANCELED,
    FAILDREADY,
    STARTGAME,
    GIVEUP,
    RIVALDISCONNECTED,
    SCOREUPDATE,
    FAILDSCOREUPDATE,
    ENDGAME
}