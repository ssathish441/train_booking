package com.sathish.thodar.features.notification;

// 'default' package-private access
class NotificationModel {
    private String receiverId; // PNR or Mobile Number
    private String headerTitle;
    private String messageBody;
    private String notificationType; // e.g., "SMS", "EMAIL", "SYSTEM_ALERT"
    private long timestampEpoch;

    public NotificationModel() {
        this.timestampEpoch = System.currentTimeMillis();
    }



    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public long getTimestampEpoch() {
        return timestampEpoch;
    }

    public void setTimestampEpoch(long timestampEpoch) {
        this.timestampEpoch = timestampEpoch;
    }
}