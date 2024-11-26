package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.github.cdimascio.dotenv.Dotenv;

public class EmulatorManager {
    private static AppiumDriverLocalService appiumService;
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String EMULATOR_NAME = dotenv.get("EMULATOR_NAME");
    private static final String DEVICE_PROFILE = dotenv.get("DEVICE_PROFILE");
    private static final String LOCAL_IMAGE_PATH = dotenv.get("LOCAL_IMAGE_PATH");
    private static final String SDK_SYSTEM_IMAGES_PATH = System.getProperty("user.home")
            + "/Library/Android/sdk/system-images/";
    private static final String APK_URL_STRING = dotenv.get("APK_PATH");

    /**
     * Verify that the local system image exists
     */
    private static void verifyLocalImagePath() {
        File imageDir = new File(LOCAL_IMAGE_PATH);
        if (!imageDir.exists() || !imageDir.isDirectory()) {
            throw new RuntimeException("System image directory not found at: " + LOCAL_IMAGE_PATH);
        }
    }

    /**
     * Copy the local system image to the SDK system-images directory.
     */
    public static void linkSystemImage() throws IOException {
        System.out.println("Linking system image from project directory...");
        verifyLocalImagePath();

        Path targetDir = Paths.get(SDK_SYSTEM_IMAGES_PATH, "android-35/google_apis_playstore/arm64-v8a");
        Path sourceDir = Paths.get(LOCAL_IMAGE_PATH);

        // Ensure target directory exists
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Copy files from source to target
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy system image files.", e);
            }
        });
        System.out.println("System image linked successfully.");
    }

    /**
     * Create a new emulator if it does not exist
     */
    public static void createEmulator() throws IOException, InterruptedException {
        System.out.println("Checking if emulator exists...");
        File avdFolder = new File(System.getProperty("user.home") + "/.android/avd/" + EMULATOR_NAME + ".avd");
        if (avdFolder.exists()) {
            System.out.println("Emulator already exists: " + EMULATOR_NAME);
            return;
        }

        System.out.println("Creating new emulator...");
        String[] command = {
                "avdmanager", "create", "avd",
                "--name", EMULATOR_NAME,
                "--device", DEVICE_PROFILE,
                "--force",
                "--package", "system-images;android-35;google_apis_playstore;arm64-v8a", // Ensure this path is correct
                "--abi", "google_apis_playstore/arm64-v8a" // Specify the ABI explicitly

        };

        System.out.println("Executing command:");
        for (String part : command) {
            System.out.print(part + " ");
        }
        System.out.println();

        Process process = Runtime.getRuntime().exec(command);

        // Capture and log standard output and error streams
        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            System.out.println("Command Output:");
            stdOutput.lines().forEach(System.out::println);

            System.out.println("Command Errors:");
            stdError.lines().forEach(System.err::println);
        }

        process.waitFor();

        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to create emulator. Exit code: " + process.exitValue());
        }

        System.out.println("Emulator created successfully.");
    }

    public static void startEmulator() throws IOException, InterruptedException {
        System.out.println("Restarting ADB server...");

        // Restart ADB server to ensure a clean state
        restartADBServer();

        System.out.println("Starting emulator...");
        String command = String.format("emulator @%s -no-snapshot-load -wipe-data", EMULATOR_NAME);

        // Start emulator and capture logs
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Threads to capture emulator logs
        Thread logThread = createLogThread("Emulator Log", inputReader);
        Thread errorLogThread = createLogThread("Emulator Error", errorReader);

        logThread.start();
        errorLogThread.start();

        System.out.println("Waiting for emulator to initialize...");
        boolean emulatorStarted = waitForEmulatorInADB(120); // Wait up to 120 seconds
        if (!emulatorStarted) {
            throw new RuntimeException(
                    "Emulator did not appear in ADB devices. Ensure the emulator is starting properly.");
        }

        System.out.println("Emulator detected. Checking for boot completion...");
        waitForEmulatorBoot(120); // Wait up to 120 seconds for boot completion

        System.out.println("Waiting for emulator UI stabilization...");
        Thread.sleep(10000); // Stabilization delay for UI

        // Join log threads with a timeout to ensure they don't block indefinitely
        logThread.join(5000); // 5 seconds timeout
        errorLogThread.join(5000); // 5 seconds timeout

        System.out.println("Emulator is running and ready for further actions.");
    }

    /**
     * Creates a thread to print logs from a BufferedReader.
     */
    private static Thread createLogThread(String logType, BufferedReader reader) {
        return new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(logType + ": " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Waits for the emulator to appear in ADB devices.
     * 
     * @param timeoutSeconds Maximum time to wait in seconds.
     * @return true if the emulator is detected, false otherwise.
     */
    private static boolean waitForEmulatorInADB(int timeoutSeconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000;

        while (System.currentTimeMillis() - startTime < timeout) {
            Process process = Runtime.getRuntime().exec("adb devices");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("emulator-5554")) {
                        System.out.println("Emulator detected in ADB devices.");
                        return true;
                    }
                }
            }
            Thread.sleep(5000); // Retry every 5 seconds
        }
        return false;
    }

    /**
     * Waits for the emulator to complete the boot process.
     * 
     * @param timeoutSeconds Maximum time to wait in seconds.
     */
    private static void waitForEmulatorBoot(int timeoutSeconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000;

        while (System.currentTimeMillis() - startTime < timeout) {
            Process bootCheck = Runtime.getRuntime().exec("adb -s emulator-5554 shell getprop sys.boot_completed");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(bootCheck.getInputStream()))) {
                String bootOutput = reader.readLine();
                if ("1".equals(bootOutput)) {
                    System.out.println("Emulator boot process completed.");
                    return;
                }
            }
            System.out.println("Emulator boot not completed yet. Retrying...");
            Thread.sleep(5000); // Retry every 5 seconds
        }
        throw new RuntimeException("Emulator failed to boot within the timeout period.");
    }

    /**
     * Restart ADB server to ensure no stale connections exist.
     */
    private static void restartADBServer() throws IOException, InterruptedException {
        Process adbKill = Runtime.getRuntime().exec("adb kill-server");
        adbKill.waitFor();

        Process adbStart = Runtime.getRuntime().exec("adb start-server");
        adbStart.waitFor();
        System.out.println("ADB server restarted successfully.");
    }

    /**
     * Check if the emulator is listed in ADB devices.
     *
     * @return true if the emulator is listed, false otherwise.
     * @throws IOException If an I/O error occurs during ADB execution.
     */
    private static boolean isEmulatorListedInADB() throws IOException {
        Process process = Runtime.getRuntime().exec("adb devices");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("emulator-5554")) { // Adjust if using a different emulator port
                    System.out.println("Emulator detected in ADB devices.");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Wait for the emulator to be ready
     */
    public static void waitForEmulator() throws IOException, InterruptedException {
        System.out.println("Waiting for emulator to boot...");
        long startTime = System.currentTimeMillis();
        long timeout = 300000; // 5 minutes

        while (System.currentTimeMillis() - startTime < timeout) {
            System.out.println("Checking ADB for connected devices...");
            Process process = Runtime.getRuntime().exec("adb devices");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
            String output = builder.toString();
            System.out.println("ADB Devices Output: " + output);

            if (output.contains("emulator-5554")) {
                System.out.println("Emulator detected. Checking if boot completed...");
                Process bootCheck = Runtime.getRuntime().exec("adb -s emulator-5554 shell getprop sys.boot_completed");
                BufferedReader bootReader = new BufferedReader(new InputStreamReader(bootCheck.getInputStream()));
                StringBuilder bootBuilder = new StringBuilder();
                while ((line = bootReader.readLine()) != null) {
                    bootBuilder.append(line);
                }
                String bootOutput = bootBuilder.toString();
                System.out.println("Boot Check Output: " + bootOutput);

                if ("1".equals(bootOutput.trim())) {
                    System.out.println("Emulator is fully booted.");

                    // Adding stabilization delay
                    System.out.println("Waiting for UI stabilization...");
                    Thread.sleep(10000); // Wait for 10 seconds

                    return;
                } else {
                    System.out.println("Boot output indicates incomplete boot: " + bootOutput.trim());
                }
            }
            System.out.println("Emulator not ready. Retrying in 5 seconds...");
            Thread.sleep(5000); // Retry every 5 seconds
        }

        throw new RuntimeException("Emulator failed to boot within the timeout period.");
    }

    public static void installApk() throws IOException, InterruptedException {
        System.out.println("Preparing to install APK...");

        String command = String.format("adb -s emulator-5554 install -r %s", APK_URL_STRING);
        System.out.println("Executing command: " + command);
        Process process = Runtime.getRuntime().exec(command);

        // Step 5: Log the output of the command
        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String output = stdOutput.lines().reduce("", String::concat);
            String error = stdError.lines().reduce("", String::concat);

            System.out.println("APK Installation Output: " + output);
            System.err.println("APK Installation Errors: " + error);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to install APK. Exit code: " + exitCode);
        }

        System.out.println("APK installed successfully.");
    }

    /**
     * Full setup: Create emulator, link system image, start emulator, and install
     * APK
     */
    public static void setupEmulatorAndInstallApp() throws IOException, InterruptedException {
        linkSystemImage();
        createEmulator();
        startEmulator();
        waitForEmulator();
        installApk();
    }

    /**
     * Check if a command exists in the system PATH.
     *
     * @param command The command to check (e.g., "sdkmanager", "avdmanager",
     *                "emulator").
     * @return true if the command exists and is executable, false otherwise.
     * @throws IOException If an I/O error occurs during the check.
     */
    public static boolean isCommandAvailable(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[] { "which", command });
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            // If the reader returns a non-empty line, the command is available
            return reader.readLine() != null;
        }
    }

    /**
     * Stops the running emulator.
     */
    public static void stopEmulator() throws IOException, InterruptedException {
        System.out.println("Stopping emulator...");

        // Use adb command to terminate the emulator
        String command = "adb emu kill";
        Process process = Runtime.getRuntime().exec(command);

        // Capture the output to ensure the command was successful
        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String output = stdOutput.lines().reduce("", String::concat);
            String error = stdError.lines().reduce("", String::concat);

            if (!output.isEmpty()) {
                System.out.println("Emulator Stop Output: " + output);
            }
            if (!error.isEmpty()) {
                System.err.println("Emulator Stop Error: " + error);
            }
        }

        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to stop the emulator. Exit code: " + process.exitValue());
        }

        System.out.println("Emulator stopped successfully.");
    }

    /**
     * Start the Appium server if it's not already running.
     */
    public static void startAppiumServer() {
        if (appiumService != null && appiumService.isRunning()) {
            System.out.println("Appium server is already running.");
            return;
        }

        System.out.println("Starting Appium server...");
        appiumService = new AppiumServiceBuilder()
                .withAppiumJS(new File(
                        "/Users/martinlichev/.nvm/versions/node/v18.17.0/lib/node_modules/appium/build/lib/main.js"))
                .withIPAddress("127.0.0.1")
                .usingPort(4723)
                .withArgument(GeneralServerFlag.ALLOW_INSECURE, "chromedriver_autodownload")
                .build();

        appiumService.start();

        if (appiumService.isRunning()) {
            System.out.println("Appium server started successfully.");
        } else {
            throw new RuntimeException("Failed to start Appium server.");
        }
    }

    /**
     * Stop the Appium server.
     */
    public static void stopAppiumServer() {
        if (appiumService != null && appiumService.isRunning()) {
            System.out.println("Stopping Appium server...");
            appiumService.stop();
            System.out.println("Appium server stopped successfully.");
        } else {
            System.out.println("Appium server is not running.");
        }
    }
}