module ispw.project.movietime {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires okhttp3;
    requires com.google.gson;
    requires com.opencsv;
    requires java.sql;
    requires org.json;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

    opens ispw.project.movietime.view.gui to javafx.fxml;

    opens ispw.project.movietime to javafx.fxml;
    exports ispw.project.movietime;

    opens ispw.project.movietime.model to com.google.gson;
    exports ispw.project.movietime.model;

    opens ispw.project.movietime.view.cli to javafx.fxml;

    opens ispw.project.movietime.controller.graphic.cli to javafx.fxml;
    exports ispw.project.movietime.controller.graphic.cli;

    opens ispw.project.movietime.controller.graphic.gui to javafx.fxml;
    exports ispw.project.movietime.controller.graphic.gui;

    exports ispw.project.movietime.dao;
    exports ispw.project.movietime.controller.graphic.cli.command;
    opens ispw.project.movietime.controller.graphic.cli.command to javafx.fxml;

}