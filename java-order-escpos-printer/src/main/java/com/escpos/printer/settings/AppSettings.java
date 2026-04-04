package com.escpos.printer.settings;

import com.escpos.printer.exceptions.ConfigurationException;
import com.escpos.printer.order.utils.StringUtils;

import io.github.cdimascio.dotenv.Dotenv;

public class AppSettings {
    private final Config config;
    private static final Dotenv ENV = Dotenv.load();

    private AppSettings() {
        this.config = new Config(
            getRequiredInt("MAX_ATTEMPTS"),
            getRequiredInt("RETRY_DELAY"),
            getRequiredInt("LINE_WIDTH"),
            getRequiredString("BASE_URL"),
            getRequiredString("AUTH_URL"),
            getRequiredString("ORDERS_URL"),
            getRequiredString("UPDATE_ORDER_URL"),
            getRequiredString("CHECK_SERVER_HEALTH"),
            getRequiredString("PRINTER_IP"),
            getRequiredInt("PRINTER_PORT"),
            getRequiredString("USERNAME"),
            getRequiredString("PASSWORD")
        );;
    }

    private static class Holder {
        private static final AppSettings INSTANCE = new AppSettings();
    }

    public static AppSettings getInstance() { return Holder.INSTANCE; }

    public Config config() { return this.config; }

    private int getRequiredInt(String key) {
        String value = ENV.get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Missing or invalid integer for key: " + key);
        }
    }

    private String getRequiredString(String key) {
        String value = ENV.get(key);

        if (StringUtils.IsNullOrBlank(value)) {
            throw new ConfigurationException("Missing or blank value for key: " + key);
        }

        return value;
    }
}
