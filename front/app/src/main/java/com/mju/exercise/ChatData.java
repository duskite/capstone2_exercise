package com.mju.exercise;


import java.util.HashMap;
import java.util.Map;

public class ChatData {
    public Map<String,Boolean> usersId = new HashMap<>();
    public Map<String,Comment> comments = new HashMap<>();

    public static class Comment {
        public String senderId;
        public String message;
        public Object timestamp;
        public String profileImgPath;
    }
}
