package ispw.project.movietime.controller.graphic.cli.command;


import ispw.project.movietime.exception.CliCommandException;
import ispw.project.movietime.exception.UserException;

public interface CliCommand {
    String execute(String args) throws CliCommandException, UserException;
}