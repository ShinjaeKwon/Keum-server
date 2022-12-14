package com.balanceup.keum.service;

import static java.util.Objects.*;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balanceup.keum.config.util.JwtTokenUtil;
import com.balanceup.keum.controller.dto.TokenDto;
import com.balanceup.keum.controller.dto.request.user.ReIssueRequest;
import com.balanceup.keum.controller.dto.request.user.UserDeleteRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameDuplicateRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameUpdateRequest;
import com.balanceup.keum.controller.dto.response.user.UserDeleteResponse;
import com.balanceup.keum.controller.dto.response.user.UserResponse;
import com.balanceup.keum.domain.User;
import com.balanceup.keum.repository.RedisRepository;
import com.balanceup.keum.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final JwtTokenUtil jwtTokenUtil;
	private final RedisRepository redisRepository;

	@Transactional
	public UserResponse updateNickname(UserNicknameUpdateRequest dto, String username) {
		String nickname = dto.getNickname();

		if (userRepository.findByNickname(nickname).isPresent()) {
			throw new IllegalStateException("이미 존재하는 닉네임입니다.");
		}

		isValidNickname(nickname);

		return UserResponse.from(userRepository.findByUsername(username)
			.orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."))
			.updateUserNickname(nickname), dto.getToken());
	}

	@Transactional(readOnly = true)
	public String duplicateNickname(UserNicknameDuplicateRequest dto) {
		isValidNickname(dto.getNickname());

		if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
			throw new IllegalStateException("이미 존재하는 닉네임입니다.");
		}

		return dto.getNickname();
	}

	public TokenDto reIssue(ReIssueRequest request, UserDetails userDetails) {
		if (!jwtTokenUtil.validateToken(request.getRefreshToken(), userDetails)) {
			throw new IllegalStateException("Refresh 토큰이 만료되었습니다.");
		}

		String username = userDetails.getUsername();
		String refreshTokenInRedis = redisRepository.getValues(username);

		if (refreshTokenInRedis == null || !isNormalRefreshToken(request, refreshTokenInRedis)) {
			throw new IllegalStateException("만료되거나 존재하지 않는 RefreshToken 입니다. 다시 로그인을 시도해주세요");
		}

		return jwtTokenUtil.generateToken(username);
	}

	@Transactional
	public UserDeleteResponse delete(UserDeleteRequest request) {
		User user = getUserByUsername(request.getUsername());
		user.withdraw();
		return UserDeleteResponse.from(user);
	}

	private User getUserByUsername(String username) {
		return checkUserByUsername(userRepository.findByUsername(username));
	}

	private static User checkUserByUsername(Optional<User> optionalUser) {
		if (optionalUser.isEmpty()) {
			throw new IllegalStateException("사용자가 정확하지 않습니다.");
		}
		return optionalUser.get();
	}

	private static boolean isNormalRefreshToken(ReIssueRequest dto, String refreshTokenInRedis) {
		return refreshTokenInRedis.equals(dto.getRefreshToken());
	}

	private static void isValidNickname(String nickname) {
		if (isNull(nickname)) {
			throw new IllegalArgumentException("닉네임이 비어있습니다.");
		}

		if (!(nickname.length() > 0 && nickname.length() < 11)) {
			throw new IllegalArgumentException("닉네임의 길이는 11자 이내여야 합니다.");
		}

		if (!nickname.matches("^[a-zA-Z0-9가-힣]*$")) {
			throw new IllegalArgumentException("닉네임은 영어, 한글, 숫자만 가능합니다.");
		}
	}

	public User findUserByUsername(String username) {
		return userRepository
			.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 username 입니다."));
	}

}

