package ru.opfr.notification.model.dto;


import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class Request {
    public String id;
    public String type;
    public String user;
    public String ip;
    public String email;
    public String content;
    public String theme;
    public MultipartFile[] files;

    @Override
    public String toString() {
        return "Request{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", user='" + user + '\'' +
                ", ip='" + ip + '\'' +
                ", email='" + email + '\'' +
                ", content='" + content + '\'' +
                ", theme='" + theme + '\'' +
                ", files=" + Optional.ofNullable(files).map(files1->files1.length).orElse(0)+
                '}';
    }
}
