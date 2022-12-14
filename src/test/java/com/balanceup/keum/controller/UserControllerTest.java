package com.balanceup.keum.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.balanceup.keum.config.auth.PrincipalDetailService;
import com.balanceup.keum.controller.dto.TokenDto;
import com.balanceup.keum.controller.dto.request.user.ReIssueRequest;
import com.balanceup.keum.controller.dto.request.user.UserDeleteRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameDuplicateRequest;
import com.balanceup.keum.controller.dto.request.user.UserNicknameUpdateRequest;
import com.balanceup.keum.controller.dto.response.user.UserDeleteResponse;
import com.balanceup.keum.controller.dto.response.user.UserResponse;
import com.balanceup.keum.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private PrincipalDetailService principalDetailService;

	@DisplayName("[API][POST] ????????? ???????????? ????????? - ??????")
	@Test
	@WithAnonymousUser
	void given_UserNicknameRequest_when_VerifyDuplicateNickname_then_ReturnOk() throws Exception {
		//given
		String nickname = "nickname";
		UserNicknameDuplicateRequest request = new UserNicknameDuplicateRequest(nickname);

		//mock
		when(userService.duplicateNickname(request)).thenReturn(nickname);

		//when & then
		mockMvc.perform(post("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("success")))
			.andExpect(jsonPath("$.message", containsString("????????? ?????? ?????? ??????")));
	}

	@DisplayName("[API][POST] ????????? ???????????? ?????????(????????? ??????) - ??????")
	@Test
	@WithAnonymousUser
	void given_DuplicateUserNicknameRequest_when_VerifyDuplicateNickname_then_ReturnBadRequest() throws Exception {
		//given
		String nickname = "nickname";
		UserNicknameDuplicateRequest request = new UserNicknameDuplicateRequest(nickname);

		//when
		when(userService.duplicateNickname(Mockito.any(UserNicknameDuplicateRequest.class))).thenThrow(
			IllegalStateException.class);

		//then
		mockMvc.perform(post("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][POST] ????????? ???????????? ?????????(????????? ?????????) - ??????")
	@Test
	@WithAnonymousUser
	void given_WrongNickname_when_VerifyDuplicateNickname_then_ReturnBadRequest() throws Exception {
		//given
		String nickname = "wrongNickname";
		UserNicknameDuplicateRequest request = new UserNicknameDuplicateRequest(nickname);

		//when
		when(userService.duplicateNickname(Mockito.any(UserNicknameDuplicateRequest.class))).thenThrow(
			IllegalArgumentException.class);

		//then
		mockMvc.perform(post("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][PUT] ????????? ???????????? ????????? - ??????")
	@Test
	@WithMockUser
	void given_UpdateNicknameRequest_when_UpdateNickname_then_ReturnOk() throws Exception {
		//given
		String userName = "userName";
		String nickname = "nickname";
		String token = "jwtToken";
		UserNicknameUpdateRequest request = new UserNicknameUpdateRequest(nickname, token);

		//mock
		when(userService.updateNickname(request, userName)).thenReturn(mock(UserResponse.class));

		//when & then
		mockMvc.perform(put("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("success")))
			.andExpect(jsonPath("$.message", containsString("????????? ???????????? ??????")));
	}

	@DisplayName("[API][PUT] ????????? ???????????? ?????????(?????? ?????????) - ??????")
	@Test
	@WithMockUser
	void given_DuplicateNickname_when_UpdateNickname_then_ReturnBadRequest() throws Exception {
		//given
		String nickname = "nickname";
		String token = "jwtToken";
		UserNicknameUpdateRequest request = new UserNicknameUpdateRequest(nickname, token);

		//mock
		when(userService.updateNickname(Mockito.any(UserNicknameUpdateRequest.class), anyString())).thenThrow(
			IllegalArgumentException.class);

		//when & then
		mockMvc.perform(put("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())

			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][PUT] ????????? ???????????? ?????????(????????? ?????????) - ??????")
	@Test
	@WithMockUser
	void given_WrongNickname_when_UpdateNickname_then_ReturnBadRequest() throws Exception {
		//given
		String nickname = "nickname";
		String token = "jwtToken";
		UserNicknameUpdateRequest request = new UserNicknameUpdateRequest(nickname, token);

		//mock
		when(userService.updateNickname(Mockito.any(UserNicknameUpdateRequest.class), anyString())).thenThrow(
			IllegalArgumentException.class);

		//when & then
		mockMvc.perform(put("/user/nickname")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())

			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][GET] ?????? ????????? ????????? ")
	@Test
	@WithMockUser
	void given_TokenDtoAndUserDetails_when_ReIssueToken_then_ReturnCreated() throws Exception {
		//given
		ReIssueRequest request = getReIssueRequestFixture();

		//mock
		when(principalDetailService.loadUserByUsername(request.getUsername())).thenReturn(mock(UserDetails.class));
		when(userService.reIssue(request, mock(UserDetails.class))).thenReturn(mock(TokenDto.class));

		//when & then
		mockMvc.perform(post("/auth/refresh")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("success")))
			.andExpect(jsonPath("$.message", containsString("AccessToken ???????????? ?????????????????????.")));
	}

	@DisplayName("[API][GET] ?????? ????????? ?????????(???????????? ?????? ??????) - ?????? ")
	@Test
	@WithMockUser
	void given_TokenDtoAndUserDetails_when_ReIssueToken_then_ReturnBadRequest() throws Exception {
		//given
		ReIssueRequest request = getReIssueRequestFixture();
		//mock
		when(principalDetailService.loadUserByUsername(request.getUsername()))
			.thenThrow(UsernameNotFoundException.class);

		//when & then
		mockMvc.perform(post("/auth/refresh")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][GET] ?????? ????????? ?????????(????????????) - ?????? ")
	@Test
	@WithMockUser
	void given_WrongToken_when_ReIssueToken_then_ReturnBadRequest() throws Exception {
		//given
		ReIssueRequest request = getReIssueRequestFixture();
		//mock
		when(principalDetailService.loadUserByUsername(request.getUsername())).thenReturn(mock(UserDetails.class));
		when(userService.reIssue(Mockito.any(ReIssueRequest.class), Mockito.any(UserDetails.class)))
			.thenThrow(IllegalStateException.class);

		//when & then
		mockMvc.perform(post("/auth/refresh")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][PUT] ?????? ?????? ????????? (???????????? ????????? ????????? ??????) - ??????")
	@Test
	void given_NonExistentUser_when_DeleteUser_then_ReturnBadRequest() throws Exception {
		//given
		String username = "username";
		UserDeleteRequest request = new UserDeleteRequest(username);

		//mock
		when(userService.delete(Mockito.any(UserDeleteRequest.class))).thenThrow(IllegalStateException.class);

		//when & then
		mockMvc.perform(put("/withdraw")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("error")));
	}

	@DisplayName("[API][PUT] ?????? ?????? ????????? - ??????")
	@Test
	void given_Username_when_DeleteUser_then_ReturnOk() throws Exception {
		//given
		String username = "username";
		UserDeleteRequest request = new UserDeleteRequest(username);

		//mock
		when(userService.delete(request)).thenReturn(mock(UserDeleteResponse.class));

		//when & then
		mockMvc.perform(put("/withdraw")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
			).andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.resultCode", containsString("success")))
			.andExpect(jsonPath("$.message", containsString("??????????????? ?????????????????????.")));
	}

	private static ReIssueRequest getReIssueRequestFixture() {
		ReIssueRequest request = new ReIssueRequest();
		request.setUsername("username");
		request.setToken("accessToken");
		request.setRefreshToken("refreshToken");
		return request;
	}

}
