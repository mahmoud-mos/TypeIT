package model;

// regular chat message containing only text
public class TextMessage extends Message {

    private final String content;

    public TextMessage(String senderUsername, String content) {
        super(senderUsername);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String format() {
        return "[" + getFormattedTime() + "] " + getSenderUsername() + ": " + content;
    }
}