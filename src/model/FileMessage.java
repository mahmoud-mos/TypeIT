package model;

// a message that carries a file as raw bytes (image, document, etc)
public class FileMessage extends Message {

    private final String fileName;
    private final byte[] fileData; // the actual file content as bytes

    public FileMessage(String senderUsername, String fileName, byte[] fileData) {
        super(senderUsername);
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    @Override
    public String format() {
        return "[" + getFormattedTime() + "] " + getSenderUsername()
                + " sent a file: " + fileName
                + " (" + fileData.length + " bytes)";
    }
}