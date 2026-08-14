package com.example.agentlearning.lab06;

/**
 * 一个任务，对应 {@code task} 表的一行。
 */
public record Task(String id, String title, String description, String status, String createdAt) {
}
