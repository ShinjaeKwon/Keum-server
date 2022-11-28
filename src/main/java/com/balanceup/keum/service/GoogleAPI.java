package com.balanceup.keum.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.balanceup.keum.config.util.JwtTokenUtil;
import com.balanceup.keum.controller.response.TokenResponse;
import com.balanceup.keum.domain.User;
import com.balanceup.keum.repository.RedisRepository;
import com.balanceup.keum.repository.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleAPI {

	private final String PROVIDER_GOOGLE = "google";
	private final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v1/userinfo";
	//TODO : CLIENT_ID yml 파일에 넣기
	private final String CLIENT_ID = "915621246164-v0qg2t6ptjk6jlj6g6hggvmnnh8ul1nd.apps.googleusercontent.com";
	private final String CLIENT_SECRET = "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";
	private final String REDIRECT_URI = "http://localhost:8080/login/google";
	private final String GRANT_TYPE = "authorization_code";

	private final UserRepository userRepository;
	private final JwtTokenUtil jwtTokenUtil;
	private final RedisRepository redisRepository;
	private final BCryptPasswordEncoder encoder;

	public String getAccessToken(String authorize_code) {
		ResponseEntity<String> response = getGoogleTokenResponse(authorize_code);
		return getElementByResponseBody(response).getAsJsonObject().get("access_token").getAsString();
	}

	public Map<String, String> getUserInfo(String accessToken) {
		JsonElement element = getElementByResponseBody(getResponseByAccessToken(accessToken));

		String password = element.getAsJsonObject().get("id").getAsString();
		String username = element.getAsJsonObject().get("email").getAsString();

		redisRepository.setValues(username, password, Duration.ofMillis(60 * 1000));

		return getHeaderUserInfo(username);
	}

	public Map<String, String> join(String username, String nickname) {
		String password = encoder.encode(redisRepository.getValues(username));
		userRepository.save(User.of(username, password, nickname, PROVIDER_GOOGLE));
		return makeTokens(username);
	}

	public Map<String, String> login(String username) {
		userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 username 입니다."));
		return makeTokens(username);
	}

	private MultiValueMap<String, String> addParamByAuthorizeCode(String authorize_code) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", GRANT_TYPE);
		params.add("client_id", CLIENT_ID);
		params.add("client_secret", CLIENT_SECRET);
		params.add("redirect_uri", REDIRECT_URI);
		params.add("code", authorize_code);
		log.info("addParamByAuthorizeCode End");
		return params;
	}

	private HttpEntity<MultiValueMap<String, String>> getOAuthTokenRequest(String authorize_code) {
		return new HttpEntity<>(addParamByAuthorizeCode(authorize_code),
			null);
	}

	private ResponseEntity<String> getResponseByAccessToken(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);

		HttpEntity request = new HttpEntity(headers);

		return new RestTemplate().exchange(
			USER_INFO_URI,
			HttpMethod.GET,
			request,
			String.class
		);
	}

	private static HttpEntity<MultiValueMap<String, String>> getUserInfoRequest(String accessToken) {
		return new HttpEntity<>(setParamByAccessToken(accessToken), null);
	}

	private static MultiValueMap<String, String> setParamByAccessToken(String accessToken) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("access_Token", accessToken);
		return params;
	}

	private ResponseEntity<String> getGoogleTokenResponse(String authorize_code) {
		return new RestTemplate().postForEntity(TOKEN_URL, getOAuthTokenRequest(authorize_code), String.class);
	}

	private JsonElement getElementByResponseBody(ResponseEntity<String> response) {
		System.out.println(response.getBody());
		return new JsonParser().parse(response.getBody());
	}

	private Map<String, String> getHeaderUserInfo(String username) {
		Map<String, String> header = new HashMap<>();
		header.put("username", username);
		return getHeaderLoginState(username, header);
	}

	private Map<String, String> getHeaderLoginState(String username, Map<String, String> header) {
		header.put("provider", PROVIDER_GOOGLE);
		if (userRepository.findByUsername(username).isPresent()) {
			header.put("login", "sign-in");
			return header;
		}
		header.put("login", "sign-up");
		return header;
	}

	private Map<String, String> putTokensMap(String username) {
		Map<String, String> tokens = new HashMap<>();
		TokenResponse token = jwtTokenUtil.generateToken(username);
		tokens.put("accessToken", token.getToken());
		tokens.put("refreshToken", token.getRefreshToken());
		return tokens;
	}

	private Map<String, String> makeTokens(String username) {
		return putTokensMap(username);
	}

}