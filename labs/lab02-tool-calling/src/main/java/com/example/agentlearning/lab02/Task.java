package com.example.agentlearning.lab02;

/**
 * 一个任务行，对应 SQLite {@code task} 表的一行。
 */
public record Task(String id, String title, String status, String createdAt) {
}
