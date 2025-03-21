package com.ct113h.social_media_api.service;


import com.ct113h.social_media_api.config.AWSConfig;
import com.ct113h.social_media_api.model.MediaType;
import com.ct113h.social_media_api.model.Post;
import com.ct113h.social_media_api.model.PostCreator;
import com.ct113h.social_media_api.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PostService   {
    private final PostRepository postRepository;
    private final S3Client s3Client;
    private final S3PresignedUrlService s3PresignedUrlService;

    public  Post createPost(String title, String text, List<String> tags, MultipartFile mediaFile) throws IOException {
        String fileName = storeFileInS3(mediaFile);
        PostCreator creator = getPostCreator();
        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setTags(tags);
        post.setLikes(0);
        post.setCreator(creator);
        post.setCreatedAt(java.time.LocalDateTime.now());
        post.setMediaUrl(fileName);
        MediaType mediaType = getMediaType(mediaFile);
        post.setMediaType(mediaType);
        return postRepository.save(post);
    }

    private static PostCreator getPostCreator() {
        var auth = (JwtAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        var claims = auth.getToken().getClaims();
        var userId = (String) claims.get("id");
        var userName = (String) claims.get("name");
        PostCreator creator = PostCreator.builder()
                .id(userId)
                .name(userName)
                .build();
        return creator;
    }

    private static MediaType getMediaType(MultipartFile mediaFile) {
        return Objects.requireNonNull(mediaFile.getContentType()).startsWith("video/") ? MediaType.VIDEO :
                (mediaFile.getContentType().startsWith("image/") ? MediaType.IMAGE : null);
    }

    private String storeFileInS3(MultipartFile mediaFile) throws IOException {
        String fileName = UUID.randomUUID().toString()+" - "+ mediaFile.getOriginalFilename();
        if(mediaFile !=null && !mediaFile.isEmpty()){
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(AWSConfig.BUCKET_NAME)
                    .key(fileName)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(mediaFile.getBytes()));
        }
        return fileName;
    }

    public Page<Post> getAllPosts(int page, int size,String searchCriteria) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        var postList =
                StringUtils.hasText(searchCriteria)? postRepository.searchByText(searchCriteria,PageRequest.of(page, size, sort))
                        :postRepository.findAll(PageRequest.of(page, size, sort));
        postList.forEach(post -> {
            if(post.getMediaUrl()!=null) {
                post.setPresignedUrl(s3PresignedUrlService.generatePresignedUrl(post.getMediaUrl()));
            }
            post.getCreator().setProfilePhoto(s3PresignedUrlService.generatePresignedUrl(post.getCreator().getId()+"-profile"));
        });
        return postList;
    }

    public Post getPostById(String id) {
        var post =  postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        if(post.getMediaUrl()!=null) {
            post.setPresignedUrl(s3PresignedUrlService.generatePresignedUrl(post.getMediaUrl()));
        }
        return post;
    }

    public Post updatePost(String id, String title, String text, List<String> tags, MultipartFile mediaFile) throws IOException {
        Post post = getPostById(id);
        if(post.getMediaUrl()!=null){
            s3Client.deleteObject(builder -> builder.bucket(AWSConfig.BUCKET_NAME).key(post.getMediaUrl()));
        }
        String fileName = storeFileInS3(mediaFile);
        post.setTitle(title);
        post.setText(text);
        post.setTags(tags);
        post.setMediaUrl(fileName);
        MediaType mediaType = getMediaType(mediaFile);
        post.setMediaType(mediaType);
        return postRepository.save(post);
    }

    public void deletePost(String id) {
        Post post = getPostById(id);
        if(post.getMediaUrl()!=null){
            s3Client.deleteObject(builder -> builder.bucket(AWSConfig.BUCKET_NAME).key(post.getMediaUrl()));
        }
        postRepository.deleteById(id);
    }


    public void likePost(String id) {
        postRepository.incrementLikes(id);
    }
}