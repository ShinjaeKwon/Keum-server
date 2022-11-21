package com.balanceup.keum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.balanceup.keum.controller.request.UpdateNicknameRequest;
import com.balanceup.keum.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class UserController {

	private final UserService userService;

	@PostMapping("/user/nickname/duplicate")
	public ResponseEntity<?> duplicateNickname(@RequestBody String nickname) {
		return new ResponseEntity<>(userService.duplicateNickname(nickname), HttpStatus.OK);
	}

	@PutMapping("/user/nickname")
	public ResponseEntity<?> updateNickname(@RequestBody UpdateNicknameRequest dto) {
		return new ResponseEntity<>(userService.updateNickname(dto), HttpStatus.OK);
	}

}
