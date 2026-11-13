package com.company.framework.mobile.driver;

import com.company.framework.mobile.config.DeviceProfile;
import com.company.framework.mobile.config.Platform;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

final class DevicePrerequisites {
    private DevicePrerequisites() {}

    static void validate(DeviceProfile profile, URI serverUrl) {
        if (profile.platform() != Platform.ANDROID || !isLocal(serverUrl)) return;
        if (profile.avd() != null && !profile.avd().isBlank()) {
            requireCommandOutput("emulator", "-list-avds", profile.avd(),
                    "Android AVD '" + profile.avd() + "' is not installed");
        }
        if (profile.udid() != null && !profile.udid().isBlank()) {
            String output = requireCommandOutput("adb", "devices", null,
                    "ADB is unavailable; cannot validate Android UDID '" + profile.udid() + "'");
            boolean connected = Arrays.stream(output.split("\\R"))
                    .anyMatch(line -> line.startsWith(profile.udid() + "\tdevice"));
            if (!connected) {
                throw new IllegalStateException("Android device '" + profile.udid()
                        + "' is not connected according to adb devices");
            }
        }
    }

    private static String requireCommandOutput(String command, String argument,
                                                String expected, String message) {
        try {
            Process process = new ProcessBuilder(command, argument)
                    .redirectErrorStream(true).start();
            if (!process.waitFor(10, TimeUnit.SECONDS) || process.exitValue() != 0) {
                throw new IllegalStateException(message);
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (expected != null && Arrays.stream(output.split("\\R"))
                    .noneMatch(line -> line.trim().equals(expected))) {
                throw new IllegalStateException(message);
            }
            return output;
        } catch (IOException exception) {
            throw new IllegalStateException(message, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(message, exception);
        }
    }

    private static boolean isLocal(URI uri) {
        return uri.getHost().equals("127.0.0.1")
                || uri.getHost().equals("localhost")
                || uri.getHost().equals("::1");
    }
}
