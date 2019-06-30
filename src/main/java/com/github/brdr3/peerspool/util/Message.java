package com.github.brdr3.peerspool.util;

import com.google.gson.Gson;
import java.io.File;
import java.util.HashMap;

public class Message {
    private Long id;
    private FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> content;
    private User from;
    private User to;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> content) {
        this.content = content;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }
    
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public static class MessageBuilder {
         
        Message m;
        
        public MessageBuilder() {
            m = new Message();
        }
        
        public MessageBuilder id(Long id) {
            m.setId(id);
            return this;
        }
        
        public MessageBuilder to(User to) {
            m.setTo(to);
            return this;
        }
        
        public MessageBuilder from(User from) {
            m.setFrom(from);
            return this;
        }
        
        public MessageBuilder content(FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> content) {
            m.setContent(content);
            return this;
        }
        
        public Message build() {
            return m;
        }
    }
}
