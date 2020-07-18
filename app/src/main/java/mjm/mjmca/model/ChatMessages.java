package mjm.mjmca.model;

public class ChatMessages {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;;
    public String date, time, messageID, name;
    public String type;
    public String downloadURl;
    public String fileName;
    public String messagePushID;
    public String fileExtension;
    public String fileSize;

    public ChatMessages(){

    }

    public ChatMessages(String sender, String receiver, String message, boolean isseen, String date, String time, String messageID, String name, String type, String downloadURl, String fileName, String messagePushID, String fileExtension, String fileSize) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.date = date;
        this.time = time;
        this.messageID = messageID;
        this.name = name;
        this.type = type;
        this.downloadURl = downloadURl;
        this.fileName = fileName;
        this.messagePushID = messagePushID;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDownloadURl() {
        return downloadURl;
    }

    public void setDownloadURl(String downloadURl) {
        this.downloadURl = downloadURl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessagePushID() {
        return messagePushID;
    }

    public void setMessagePushID(String messagePushID) {
        this.messagePushID = messagePushID;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
