package gainnim.fight.controller

import gainnim.fight.dto.request.TrainingHistoryRequest
import gainnim.fight.dto.response.TrainingHistoryResponse
import gainnim.fight.entity.TrainingType
import gainnim.fight.service.TrainingHistoryService
import gainnim.fight.util.ResponseFormat
import gainnim.fight.util.ResponseFormatBuilder
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/training")
class TrainingHistoryController(val trainingHistoryService: TrainingHistoryService) {
    @GetMapping
    fun getMyTrainingHistory(principal: Principal, @Param(value = "trainingType") trainingType: TrainingType): ResponseEntity<ResponseFormat<List<TrainingHistoryResponse>>> {
        val userId = UUID.fromString(principal.name)
        val result = trainingHistoryService.getMyTrainingHistory(userId, trainingType)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.build(result))
    }
    @PostMapping
    fun postTrainingHistory(principal: Principal, request: TrainingHistoryRequest): ResponseEntity<ResponseFormat<Any>> {
        val userId = UUID.fromString(principal.name)
        trainingHistoryService.postTrainingHistory(userId, request)
        return ResponseEntity.ok(ResponseFormatBuilder { "message" }.noData())
    }
}