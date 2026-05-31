import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class CodeInspector {

    // Шаговый индикатор
    public static void stepTracker(String действие) {
        System.out.println("[STEP] Сейчас происходит: " + действие);
        LogManager.record("[STEP] " + действие);
    }

    // Прогресс копирования
    public static void showCopyProgress(long сделано, long всего) {
        int percent = (int) ((сделано * 100) / всего);
        System.out.println("Прогресс: " + percent + "%");
        LogManager.record("Прогресс копирования: " + percent + "%");
    }

    // Бинарный поиск проблем
    public static void findProblemZone(Path file) throws IOException {
        LogManager.record("Запуск поиска проблем в файле: " + file.getFileName());

        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {

            long left = 0;
            long right = channel.size();
            ByteBuffer buffer = ByteBuffer.allocate(1);
            int checkCount = 0;

            while (left < right) {
                long mid = (left + right) / 2;
                channel.position(mid);
                buffer.clear();
                int read = channel.read(buffer);

                if (read == -1) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
                checkCount++;
                System.out.println("Проверка #" + checkCount + " на позиции: " + mid);
            }

            System.out.println("Проблемная зона около позиции: " + left);
            LogManager.record("Бинарный поиск завершён. Проблемная позиция: " + left);
        }
    }
}