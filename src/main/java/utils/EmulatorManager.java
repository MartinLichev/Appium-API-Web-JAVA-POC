package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import io.github.cdimascio.dotenv.Dotenv;

public class EmulatorManager {
    private static final Dotenv dotenv = Dotenv.configure().load();

    private static final String EMULATOR_NAME = dotenv.get("EMULATOR_NAME");
    private static final String APK_PATH = dotenv.get("APK_PATH");
    private static final String DEVICE_PROFILE = dotenv.get("DEVICE_PROFILE");
    private static final String LOCAL_IMAGE_PATH = dotenv.get("LOCAL_IMAGE_PATH");
    private static final String SDK_SYSTEM_IMAGES_PATH = System.getProperty("user.home") + "/Library/Android/sdk/system-images/";

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

    /**
     * Start the emulator
     */
    public static void startEmulator() throws IOException, InterruptedException {
        System.out.println("Restarting ADB server...");
        Process adbKill = Runtime.getRuntime().exec("adb kill-server");
        adbKill.waitFor();
    
        Process adbStart = Runtime.getRuntime().exec("adb start-server");
        adbStart.waitFor();
        System.out.println("ADB server restarted successfully.");
        
        System.out.println("Starting emulator...");
        String command = String.format("emulator @%s -no-snapshot-load -wipe-data", EMULATOR_NAME);

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        while ((line = inputReader.readLine()) != null) {
            System.out.println("Emulator Log: " + line);
        }
        while ((line = errorReader.readLine()) != null) {
            System.err.println("Emulator Error: " + line);
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to start emulator. Check logs for details.");
        }
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
                    return;
                }
            }
            System.out.println("Emulator not ready. Retrying in 5 seconds...");
            Thread.sleep(5000); // Retry every 5 seconds
        }
    
        throw new RuntimeException("Emulator failed to boot within the timeout period.");
    }
    

    /**
     * Install APK on the emulator
     */
    public static void installApk() throws IOException, InterruptedException {
        System.out.println("Installing APK...");
        String command = String.format("adb -s emulator-5554 install -r %s", APK_PATH);
    
        System.out.println("Executing command: " + command);
        Process process = Runtime.getRuntime().exec(command);
    
        // Log the output of the command
        BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
        System.out.println("Command Output:");
        stdOutput.lines().forEach(System.out::println);
    
        System.out.println("Command Errors:");
        stdError.lines().forEach(System.err::println);
    
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to install APK. Exit code: " + exitCode);
        }
    
        System.out.println("APK installed successfully.");
    }

    /**
     * Full setup: Create emulator, link system image, start emulator, and install APK
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
 * @param command The command to check (e.g., "sdkmanager", "avdmanager", "emulator").
 * @return true if the command exists and is executable, false otherwise.
 * @throws IOException If an I/O error occurs during the check.
 */
public static boolean isCommandAvailable(String command) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"which", command});
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        // If the reader returns a non-empty line, the command is available
        return reader.readLine() != null;
    }
}
}