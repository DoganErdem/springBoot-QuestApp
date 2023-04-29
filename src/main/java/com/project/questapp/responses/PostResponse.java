package com.project.questapp.responses;

import com.project.questapp.entities.Like;
import com.project.questapp.entities.Post;
import lombok.Data;

import java.util.List;

@Data
public class PostResponse {

    Long id;
    Long userId;
    String userName;
    String text;
    String title;
    List<LikeResponse> postLikes;

    public PostResponse(Post entity, List<LikeResponse> likes){
        this.id =entity.getId();
        this.userId = entity.getUser().getId();
        this.userName = entity.getUser().getUsername();
        this.text = entity.getText();
        this.title = entity.getTitle();
        this.postLikes = likes;
    }
}
