package com.example.dotdot.domain.task;
public enum TaskStatus {
    TODO("대기"),
    PROCESSING("진행"),
    DONE("완료");

    private final String ko;

    TaskStatus(String ko) { this.ko = ko; }

    public String getKo() { return ko; }
}
