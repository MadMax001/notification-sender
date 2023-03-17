package ru.opfr.notification.model.dto;


import org.springframework.web.multipart.MultipartFile;

public class Request {
    public String id;
    public String type;
    public String user;
    public String ip;
    public String email;
    public String content;
    public String theme;
    public MultipartFile[] files;
}
