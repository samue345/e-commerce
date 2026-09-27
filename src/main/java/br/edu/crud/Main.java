package br.edu.crud;

import io.javalin.Javalin;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Javalin app = config.createApplication();
        app.start(config.httpPort());
    }
}
