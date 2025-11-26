package com.example.chatapp.pattern.command;

/**
 * Command interface for executing various actions
 * Part of Command Pattern implementation
 */
public interface Command {
    void execute();
    void undo();
    String getCommandName();
}
