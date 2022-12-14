package com.balanceup.keum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.balanceup.keum.config.auth.PrincipalDetailService;
import com.balanceup.keum.controller.dto.request.user.ReIssueRequest;
import com.balanceup.keum.controller.dto.request.user.UserDeleteRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameDuplicateRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameUpdateRequest;
import com.balanceup.keum.controller.dto.response.Response;
import com.balanceup.keum.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class UserController {

	private final UserService userService;
	private final PrincipalDetailService principalDetailService;

	@PostMapping("/user/nickname")
	public ResponseEntity<?> duplicateNickname(@RequestBody UserNicknameDuplicateRequest request) {
		return new ResponseEntity<>(getSuccessResponse("닉네임 중복 확인 성공", userService.duplicateNickname(request)),
			HttpStatus.OK);
	}

	@PutMapping("/user/nickname")
	public ResponseEntity<?> updateNickname(@RequestBody UserNicknameUpdateRequest request) {
		String username = getUserNameBySecurityContextHolder();
		return new ResponseEntity<>(getSuccessResponse("닉네임 업데이트 성공", userService.updateNickname(request, username)),
			HttpStatus.OK);
	}

	@PostMapping("/auth/refresh")
	public ResponseEntity<?> getRefreshToken(@RequestBody ReIssueRequest request) {
		UserDetails userDetails = principalDetailService.loadUserByUsername(request.getUsername());

		return new ResponseEntity<>(
			getSuccessResponse("AccessToken 재발급이 완료되었습니다.", userService.reIssue(request, userDetails)),
			HttpStatus.CREATED);
	}

	@PutMapping("/withdraw")
	public ResponseEntity<?> deleteUser(@RequestBody UserDeleteRequest request) {
		return new ResponseEntity<>(
			getSuccessResponse("회원탈퇴가 완료되었습니다.", userService.delete(request)),
			HttpStatus.OK);
	}

	private static String getUserNameBySecurityContextHolder() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	private static Response<Object> getSuccessResponse(String message, Object body) {
		return Response.of("success", message, body);
	}

}

