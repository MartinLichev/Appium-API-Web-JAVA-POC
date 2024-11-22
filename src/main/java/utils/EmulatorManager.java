package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.cdimascio.dotenv.Dotenv;

public class EmulatorManager {
    private static final Dotenv dotenv = Dotenv.configure().load();

    // Fetch these values from the .env file
    private static final String EMULATOR_NAME = dotenv.get("EMULATOR_NAME");
    private static final String APK_PATH = dotenv.get("APK_PATH");
    private static final String DEVICE_PROFILE = dotenv.get("DEVICE_PROFILE"); // Default device profile
    private static final String SYSTEM_IMAGE = dotenv.get("SYSTEM_IMAGE"); // Default image

    /**
     * Check if a command exists
     */
    public static boolean isCommandAvailable(String command) throws IOException {
        Process process = Runtime.getRuntime().exec("which " + command);
        return process.getInputStream().read() != -1;
    }

    /**
     * Install the required system image
     */
    public static void installSystemImage() throws IOException, InterruptedException {
        System.out.println("Checking if system image is installed...");
        Process process = Runtime.getRuntime().exec("sdkmanager --list");
        
        // Capture the output and error streams for better debugging
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        boolean imageFound = false;
    
        // Process standard output
        while ((line = inputReader.readLine()) != null) {
            System.out.println("SDK Manager Output: " + line);
            if (line.contains(SYSTEM_IMAGE)) {
                imageFound = true;
                break;
            }
        }
    
        // Process error output
        while ((line = errorReader.readLine()) != null) {
            System.err.println("SDK Manager Error: " + line);
        }
    
        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to list system images. Check SDK Manager logs.");
        }
    
        if (!imageFound) {
            System.out.println("System image not found. Installing...");
            String installCommand = "sdkmanager --install \"" + SYSTEM_IMAGE + "\"";
            process = Runtime.getRuntime().exec(installCommand);
    
            // Capture installation output and errors
            inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
            while ((line = inputReader.readLine()) != null) {
                System.out.println("Install Output: " + line);
            }
    
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Install Error: " + line);
            }
    
            // Wait for installation to complete
            exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to install system image. Check installation logs.");
            }
    
            System.out.println("System image installed successfully.");
        } else {
            System.out.println("System image is already installed.");
        }
    }

    /**
     * Check if the emulator is running
     */
    public static boolean isEmulatorRunning() throws IOException {
        Process process = Runtime.getRuntime().exec("adb devices");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(EMULATOR_NAME)) {
                return true;
            }
        }
        return false;
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
            "--package", SYSTEM_IMAGE,
            "--force"
        };
        Process process = Runtime.getRuntime().exec(command);
    
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println("AVD Creation Output: " + line);
        }
    
        while ((line = error.readLine()) != null) {
            System.err.println("AVD Creation Error: " + line);
        }
    
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to create emulator. Check logs for details.");
        }
    
        System.out.println("Emulator created successfully: " + EMULATOR_NAME);
    }

    /**
     * Start the emulator
     */
    public static void startEmulator() throws IOException, InterruptedException {
        System.out.println("Starting emulator...");
        String command = "emulator @" + EMULATOR_NAME + " -no-snapshot-load -wipe-data";
        Process process = Runtime.getRuntime().exec(command);
    
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = errorReader.readLine()) != null) {
            System.err.println("Emulator Log: " + line);
        }
    
        Thread.sleep(30000); // Wait for the emulator to boot up
        System.out.println("Emulator started.");
    }

    /**
     * Wait for the emulator to be ready
     */
    public static void waitForEmulator() throws IOException, InterruptedException {
        System.out.println("Waiting for emulator to be fully booted...");
        
        long startTime = System.currentTimeMillis();
        long timeout = 120000; // 2 minutes
    
        // Check if the emulator is connected
        while (System.currentTimeMillis() - startTime < timeout) {
            Process process = Runtime.getRuntime().exec("adb devices");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean emulatorFound = false;
    
            while ((line = reader.readLine()) != null) {
                if (line.contains("emulator-5554")) {
                    emulatorFound = true;
                    break;
                }
            }
    
            if (!emulatorFound) {
                System.out.println("Emulator not yet connected. Retrying...");
                Thread.sleep(5000);
                continue;
            }
    
            // Check if boot completed
            process = Runtime.getRuntime().exec("adb -s emulator-5554 shell getprop sys.boot_completed");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                if ("1".equals(line.trim())) {
                    System.out.println("Emulator is fully booted.");
                    return;
                }
            }
    
            System.out.println("Emulator boot in progress. Retrying...");
            Thread.sleep(5000); // Retry every 5 seconds
        }
    
        throw new RuntimeException("Emulator failed to boot within timeout.");
    }

    /**
     * Install the APK on the emulator
     */
    public static void installApk() throws IOException, InterruptedException {
        System.out.println("Installing APK on the emulator...");
        Process process = Runtime.getRuntime().exec("adb -s " + EMULATOR_NAME + " install -r " + APK_PATH);
        process.waitFor();
        System.out.println("APK installed.");
    }

    /**
     * Setup emulator and install APK
     */
    public static void setupEmulatorAndInstallApp() throws IOException, InterruptedException {
        if (!isCommandAvailable("sdkmanager") || !isCommandAvailable("avdmanager") || !isCommandAvailable("emulator")) {
            throw new RuntimeException("Required tools (sdkmanager, avdmanager, emulator) are not available in PATH.");
        }

        installSystemImage();
        createEmulator();

        if (!isEmulatorRunning()) {
            startEmulator();
            waitForEmulator();
        } else {
            System.out.println("Emulator is already running.");
        }

        installApk();
    }
}

