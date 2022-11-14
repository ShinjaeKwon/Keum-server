package com.balanceup.keum.config.oauth;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

	private Map<String, Object> attributes;
	private Map<String, Object> kakaoAccount;

	public KakaoUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
		this.kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
	}

	@Override
	public String getProviderId() {
		return String.valueOf(attributes.get("id"));
	}

	@Override
	public String getProvider() {
		return "kakao";
	}

	@Override
	public String getEmail() {
		return String.valueOf(kakaoAccount.get("email"));
	}

	@Override
	public String getName() {
		return String.valueOf(attributes.get("name"));
	}
}
