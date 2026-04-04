package com.escpos.printer.order;

import com.escpos.printer.settings.AppSettings;
import com.escpos.printer.settings.Config;

public class Main {
    public static void main(String[] args) {
        AppSettings settings = AppSettings.getInstance();
        Config config = settings.config();

        System.out.println(config);

    }
}