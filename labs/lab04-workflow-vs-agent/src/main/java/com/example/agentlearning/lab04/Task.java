package com.example.agentlearning.lab04;

/**
 * 一个任务行，对应 SQLite {@code task} 表的一行。
 */
public record Task(String id, String title, String description, String status, String createdAt) {
}
