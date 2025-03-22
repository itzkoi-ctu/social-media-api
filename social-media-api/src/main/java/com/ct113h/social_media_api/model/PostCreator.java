package com.ct113h.social_media_api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCreator {
    private String id;
    private String name;
    private String profilePhoto;
}