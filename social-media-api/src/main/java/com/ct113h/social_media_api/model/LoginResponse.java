package com.ct113h.social_media_api.model;

public record LoginResponse(String token, String name,String email, String profilePhoto) {
}
