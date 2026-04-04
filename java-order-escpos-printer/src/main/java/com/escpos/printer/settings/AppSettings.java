package com.escpos.printer.settings;


import io.github.cdimascio.dotenv.Dotenv;

public class AppSettings {
    private final Config config;

    private AppSettings() {
         // Carrega o arquivo .env
        Dotenv dotenv = Dotenv.load();


        this.config = new Config(
            null,
            0,
            dotenv.get("USERNAME"),
            dotenv.get("PASSWORD")
        );
    }

    private static class Holder {
        private static final AppSettings INSTANCE = new AppSettings();
    }

    public static AppSettings getInstance() { return Holder.INSTANCE; }

    public Config config() { return this.config; }
}
