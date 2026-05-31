package com.sathish.thodar.features.filemanagement;

public class FileModel {
    private String backupDirectory;
    private String trainsFile;
    private String schedulesFile;
    private String ticketsFile;

    public FileModel() {
        this.backupDirectory = "thodar_data/";
        this.trainsFile = "trains.dat";
        this.schedulesFile = "schedules.dat";
        this.ticketsFile = "tickets.dat";
    }


    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public String getTrainsFile() {
        return trainsFile;
    }

    public void setTrainsFile(String trainsFile) {
        this.trainsFile = trainsFile;
    }

    public String getSchedulesFile() {
        return schedulesFile;
    }

    public void setSchedulesFile(String schedulesFile) {
        this.schedulesFile = schedulesFile;
    }

    public String getTicketsFile() {
        return ticketsFile;
    }

    public void setTicketsFile(String ticketsFile) {
        this.ticketsFile = ticketsFile;
    }
}