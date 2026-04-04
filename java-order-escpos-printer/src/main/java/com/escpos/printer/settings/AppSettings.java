package com.escpos.printer.settings;

import com.escpos.printer.exceptions.ConfigurationException;
import com.escpos.printer.order.utils.StringUtils;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * A thread-safe Singleton class that manages the application configuration.
 * * <p>This class loads environmental variables from a {@code .env} file using the 
 * {@code Dotenv} library and maps them to an immutable {@link Config} record. 
 * It uses the "Initialization-on-demand Holder" pattern to ensure that the 
 * configuration is loaded only once and is available globally.</p>
 * * <p><strong>Fail-Fast Validation:</strong> If any required key is missing, 
 * blank, or incorrectly formatted (e.g., a non-numeric port), the class 
 * throws a {@link ConfigurationException} during initialization to prevent 
 * the application from running with invalid settings.</p>
 */
public class AppSettings {

    /** The validated, immutable configuration data. */
    private final Config config;

    /** Static instance of the environment loader. */
    private static final Dotenv ENV = Dotenv.load();

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes the {@link Config} record by fetching and validating 
     * environment variables.
     * * @throws ConfigurationException if validation or parsing fails.
     */
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
        );
    }

    /**
     * Lazy-loaded holder for the Singleton instance.
     */
    private static class Holder {
        private static final AppSettings INSTANCE = new AppSettings();
    }

    /**
     * Returns the global instance of the application settings.
     * * @return The Singleton {@code AppSettings} instance.
     * @throws ExceptionInInitializerError if the configuration fails to load 
     * on the first access.
     */
    public static AppSettings getInstance() { 
        return Holder.INSTANCE; 
    }

    /**
     * Provides access to the underlying configuration record.
     * * @return The {@link Config} object.
     */
    public Config config() { 
        return this.config; 
    }

    /**
     * Retrieves an integer value from the environment and validates its format.
     * * @param key The environment variable key.
     * @return The parsed integer value.
     * @throws ConfigurationException if the value is missing, blank, or not an integer.
     */
    private int getRequiredInt(String key) {
        String value = ENV.get(key);
        
        if (StringUtils.IsNullOrBlank(value)) {
            throw new ConfigurationException("Missing or blank value for key: " + key);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Key '" + key + "' must be a valid integer, but found: " + value);
        }
    }

    /**
     * Retrieves a string value from the environment and ensures it is not blank.
     * * @param key The environment variable key.
     * @return The string value if valid.
     * @throws ConfigurationException if the value is missing or consists only of whitespace.
     */
    private String getRequiredString(String key) {
        String value = ENV.get(key);

        if (StringUtils.IsNullOrBlank(value)) {
            throw new ConfigurationException("Missing or blank value for key: " + key);
        }

        return value;
    }
}