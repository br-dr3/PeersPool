package com.github.brdr3.peerspool.util;
public class Message {
    private Long id;
    private Object content;
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

    public void setContent(Object content) {
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
        
        public MessageBuilder content(Object content) {
            m.setContent(content);
            return this;
        }
        
        public Message build() {
            return m;
        }
    }
}
