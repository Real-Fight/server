package gainnim.fight.service

import gainnim.fight.dto.request.LoginRequest
import gainnim.fight.dto.request.SignUpRequest
import gainnim.fight.dto.response.LoginResponse
import gainnim.fight.entity.User
import gainnim.fight.repository.UserRepository
import gainnim.fight.util.error.CustomError
import gainnim.fight.util.error.ErrorState
import gainnim.fight.util.jwt.JwtProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(val userRepository: UserRepository, val passwordEncoder: BCryptPasswordEncoder, val jwtProvider: JwtProvider) {
    fun signUp(request: SignUpRequest) {
        userRepository.findUserByName(request.name)?.let { throw CustomError(ErrorState.NAME_IS_ALREADY_USED) }
        userRepository.findUserByLoginId(request.loginId)?.let { throw CustomError(ErrorState.ID_IS_ALREADY_USED) }
        val user = User(
                name = request.name,
                loginId = request.loginId,
                password = passwordEncoder.encode(request.password)
        )
        userRepository.save(user)
    }
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findUserByLoginId(request.loginId) ?: throw CustomError(ErrorState.NOT_FOUND_ID)
        if (!passwordEncoder.matches(request.password, user.password)) throw CustomError(ErrorState.WRONG_PASSWORD)
        val accessToken = jwtProvider.createToken(user.id)
        return LoginResponse(accessToken = accessToken)
    }
}