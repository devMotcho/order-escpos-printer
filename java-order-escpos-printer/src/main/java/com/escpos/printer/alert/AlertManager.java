package com.escpos.printer.alert;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class AlertManager {
    private volatile boolean alerting = false;
    private Thread alertThread;

    public synchronized void startAlert() {
        if (alerting) return;
        alerting = true;
        
        alertThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                byte[] buf = new byte[1];
                int i = 0;
                while (alerting) {
                    double angle = i / (44100.0 / 440.0) * 2.0 * Math.PI;
                    buf[0] = (byte)(Math.sin(angle) * 127.0);
                    line.write(buf, 0, 1);
                    i++;
                    
                    // Modulate to make it a beep-beep sound
                    if (i % 22050 == 0) {
                        Thread.sleep(200);
                    }
                }
                line.drain();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        alertThread.setDaemon(true);
        alertThread.start();
    }

    public synchronized void stopAlert() {
        alerting = false;
        if (alertThread != null) {
            alertThread.interrupt();
        }
    }
}
