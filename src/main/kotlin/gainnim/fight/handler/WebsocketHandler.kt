package gainnim.fight.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
class WebsocketHandler(val jwtProvider: JwtProvider): TextWebSocketHandler() {
    val log = LoggerFactory.getLogger(javaClass)
    val sessions: MutableMap<UUID, WebSocketSession> = mutableMapOf() // user -> id, session
    val userIds: MutableMap<WebSocketSession, UUID> = mutableMapOf() // user -> session, id
    val matchQueue: MutableMap<String, Queue<UUID>> = mutableMapOf(
            "SHORTSQUAT" to LinkedList(),
            "MIDDLESQUAT" to LinkedList(),
            "LONGSQUAT" to LinkedList(),
            "SHORTPUSHUP" to LinkedList(),
            "MIDDLEPUSHUP" to LinkedList(),
            "LONGPUSHUP" to LinkedList()
    )
    val rooms: MutableMap<UUID, Room> = mutableMapOf()
    val endTimestamps: MutableMap<UUID, Timestamp> = mutableMapOf() // roomId, endTimestamp
    val objectMapper = jacksonObjectMapper()
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = objectMapper.readValue<Message>(message.payload)
            log.info(message.payload)
            log.info(payload.event.name)
            when (payload.event.name) {
                "RANDOMMATCH" -> randomMatch(session, payload.data["token"] as String, MatchType.valueOf(payload.data["matchType"] as String))
                "MATCHINGCANCEL" -> matchingCancel(session)
                "READIED" -> readied(session, UUID.fromString(payload.data["roomId"] as String))
                "SCOREUPDATE" -> scoreUpdate(session, ScoreUpdateData.fromMap(payload.data))
            }
        } catch (e: Exception) {
            sendMessage(session, Event.BADREQUEST, mapOf())
        }
    }
    fun randomMatch(session: WebSocketSession, token: String, matchType: MatchType) {
        log.info(matchType.name)
        try {
            val userId = jwtProvider.getUuidByToken(token)
            sessions[userId]?.let { throw Exception() }
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
                sendMessage(session, Event.MATCHED, MatchedData(
                        rivalId = matchedUserId,
                        roomId = roomId
                ).toMap())
                sendMessage(sessions[matchedUserId]!!, Event.MATCHED, MatchedData(
                        rivalId = userId,
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
        sendMessage(session, Event.MATCHINGCANCELOK, mapOf())
    }
    fun readied(session: WebSocketSession, roomId: UUID) {
        try {
            val userId = userIds[session]!! // todo remove !!
            val room = rooms[roomId]!! // todo remove !!
            if (!room.scores.containsKey(userId)) throw Exception()
            val rivals = room.users.filter { it.key != userId } // todo optimization
            if (rivals.containsValue(UserStatus.READY)) { // todo test
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
                sendMessage(session, Event.STARTGAME, messageData)
                rivals.keys.map { sendMessage(sessions[it]!!, Event.STARTGAME, messageData) }
            }
        } catch (e: Exception) {
            sendMessage(session, Event.FAILDREADY, mapOf())
        }

    }
    fun scoreUpdate(session: WebSocketSession, data: ScoreUpdateData) { // todo optimization
        try {
            val userId = data.id
            val room = rooms[data.roomId]!!
            room.scores[userId] = data.score
            room.users.filter { it.key != userId }.keys.map { sendMessage(sessions[it]!!, Event.SCOREUPDATE, data.toMap()) } // todo remove !!
        } catch (e: Exception) {
            sendMessage(session, Event.FAILDSCOREUPDATE, mapOf())
        }
    }
    @Scheduled(fixedRate = 100)
    fun checkRoomEndTimestamp() {
        endTimestamps.map {
            if (it.value < Timestamp(System.currentTimeMillis())) {
                log.info("checkRoomEndTimestamp")
                val room = rooms[it.key]!!
                val winner = room.scores.maxBy { it.value }
                val loser = room.scores.minBy { it.value }  // todo optimization
                var draw: Boolean = false
                if (winner.value == loser.value) {
                    draw = true
                }
                room.users.keys.map {
                    sendMessage(sessions[it]!!, Event.ENDGAME,  // todo remove !!
                            mapOf(
                                    "winner" to winner,
                                    "loser" to loser,
                                    "draw" to draw
                            )
                    )
                    try {
                        val session = sessions[it]!!  // todo remove !!
                        userIds.remove(session)
                        sessions.remove(it)
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
        } catch (e: Exception) {}
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
        val roomId: UUID
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
                "rivalId" to rivalId,
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
        val socreCangeVolumn: Int,
        val id: UUID
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
                "roomId" to roomId,
                "timestamp" to timestamp,
                "score" to score,
                "socreCangeVolumn" to socreCangeVolumn,
                "id" to id
        )
    }
    companion object {
        fun fromMap(data: Map<String, Any>): ScoreUpdateData {
            return ScoreUpdateData(
                    roomId = UUID.fromString(data["roomId"] as String),
                    timestamp = Timestamp(data["timestamp"] as Long),
                    score = data["score"] as Int,
                    socreCangeVolumn = data["socreCangeVolumn"] as Int,
                    id = UUID.fromString(data["id"] as String)
            )
        }
    }
}
enum class MatchType(val length: Int) {
    SHORTSQUAT(30000), MIDDLESQUAT(300000), LONGSQUAT(1800000), SHORTPUSHUP(30000), MIDDLEPUSHUP(300000), LONGPUSHUP(1800000);
    companion object {
        fun fromOrdinal(index: Int): MatchType {
            return values()[index]
        }
    }
}
enum class Status() {
    READY, PROGRESS
}
enum class UserStatus() {
    READY, ON
}
enum class Event() {
    BADREQUEST, RANDOMMATCH, MATCHINGCANCEL, MATCHINGCANCELOK, MATCHOK, FAILDMATCH, MATCHED, READIED, FAILDREADY, STARTGAME, SCOREUPDATE, FAILDSCOREUPDATE, ENDGAME
}