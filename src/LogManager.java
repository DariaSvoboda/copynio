import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class LogManager {

    private static final Path LOG_FILE = Paths.get("log_file.txt");

    public static void record(String message) {
        String logMessage = "[" + LocalDateTime.now() + "] " + message;
        System.out.println(logMessage);

        try {
            Files.writeString(LOG_FILE, logMessage + "\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }

    public static void recordError(Exception e) {
        record("ОШИБКА: " + e.getMessage());
    }

    public static long getTimer() {
        return System.nanoTime();
    }

    public static void showTimer(String операция, long startTime) {
        long end = System.nanoTime();
        record(операция + " заняло " + ((end - startTime) / 1_000_000) + " мс");
    }
}