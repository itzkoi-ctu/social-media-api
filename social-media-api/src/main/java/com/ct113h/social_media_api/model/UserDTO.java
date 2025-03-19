package com.ct113h.social_media_api.model;

import org.springframework.web.multipart.MultipartFile;

public record UserDTO(String name, String email, String password, MultipartFile profilePhoto) {
}
