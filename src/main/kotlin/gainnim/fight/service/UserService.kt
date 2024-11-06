package gainnim.fight.service

import gainnim.fight.dto.request.UserStatusMessageRequest
import gainnim.fight.dto.response.UserInfoResponse
import gainnim.fight.repository.UserRepository
import gainnim.fight.util.error.CustomError
import gainnim.fight.util.error.ErrorState
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class UserService(val userRepository: UserRepository, val rankingService: RankingService, val s3UploadService: S3UploadService) {
    fun getMyInfo(userId: UUID): UserInfoResponse {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        val ranking = rankingService.getRankingFromUserId(userId)
        return UserInfoResponse.fromUser(user, ranking)
    }
    fun patchUserStatusMessage(userId: UUID, request: UserStatusMessageRequest) {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        userRepository.save(user.copy(statusMessage = request.statusMessage))
    }
    fun patchUserImg(userId: UUID, image: MultipartFile) {
        val user = userRepository.findUserById(userId) ?: throw CustomError(ErrorState.NOT_FOUND_USER)
        val profileImgUrl = s3UploadService.saveImage(image, "profile_image")
        userRepository.save(
                user.copy(
                        profileImgUrl = profileImgUrl
                )
        )
    }
}