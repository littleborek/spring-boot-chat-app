package com.example.chatapp.pattern.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Invoker class that executes commands and maintains command history
 * Supports undo functionality
 */
@Slf4j
@Component
public class CommandInvoker {
    
    private final List<Command> commandHistory = new ArrayList<>();
    
    /**
     * Execute a command and add it to history
     */
    public void executeCommand(Command command) {
        command.execute();
        commandHistory.add(command);
        log.info("Command executed: {}", command.getCommandName());
    }
    
    /**
     * Undo the last command
     */
    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.remove(commandHistory.size() - 1);
            lastCommand.undo();
            log.info("Command undone: {}", lastCommand.getCommandName());
        }
    }
    
    /**
     * Get command history
     */
    public List<Command> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }
    
    /**
     * Clear command history
     */
    public void clearHistory() {
        commandHistory.clear();
    }
}
