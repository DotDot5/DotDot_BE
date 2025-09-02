package com.example.dotdot.domain.task;

public enum TaskPriority {
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음");

    private final String ko;

    TaskPriority(String ko) { this.ko = ko; }

    public String getKo() { return ko; }
}
