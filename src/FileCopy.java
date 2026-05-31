import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class FileCopy {

    private static void copyUsingNIO(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void sequentialCopy(File source1, File dest1, File source2, File dest2) throws IOException {
        LogManager.record("Начало последовательного копирования");

        long startTimer = LogManager.getTimer();

        CodeInspector.stepTracker("Копирование первого файла");
        copyUsingNIO(source1, dest1);
        LogManager.record("Первый файл: " + source1.getName() + " -> " + dest1.getName());

        CodeInspector.stepTracker("Копирование второго файла");
        copyUsingNIO(source2, dest2);
        LogManager.record("Второй файл: " + source2.getName() + " -> " + dest2.getName());

        LogManager.showTimer("Последовательное копирование", startTimer);
        LogManager.record("Последовательное копирование завершено");
    }

    private static void showProgressForCopy(File source, File dest, String name) throws IOException {
        long fileSize = source.length();
        long copied = 0;

        try (FileChannel inChannel = FileChannel.open(source.toPath(), StandardOpenOption.READ);
             FileChannel outChannel = FileChannel.open(dest.toPath(),
                     StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer buffer = ByteBuffer.allocate(4096);

            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                outChannel.write(buffer);
                copied += buffer.limit();
                CodeInspector.showCopyProgress(copied, fileSize);
                buffer.clear();
            }
        }
    }

    private static void parallelCopy(File source1, File dest1, File source2, File dest2) throws InterruptedException, IOException {
        LogManager.record("Начало параллельного копирования");

        long startTimer = LogManager.getTimer();

        Thread thread1 = new Thread(() -> {
            try {
                CodeInspector.stepTracker("Поток 1: копирование " + source1.getName());
                showProgressForCopy(source1, dest1, "файл1");
                LogManager.record("Поток 1 завершён: " + source1.getName() + " -> " + dest1.getName());
            } catch (IOException e) {
                LogManager.recordError(e);
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                CodeInspector.stepTracker("Поток 2: копирование " + source2.getName());
                showProgressForCopy(source2, dest2, "файл2");
                LogManager.record("Поток 2 завершён: " + source2.getName() + " -> " + dest2.getName());
            } catch (IOException e) {
                LogManager.recordError(e);
            }
        });

        CodeInspector.stepTracker("Запуск двух потоков одновременно");
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LogManager.showTimer("Параллельное копирование", startTimer);
        LogManager.record("Параллельнео копирование завершено");
    }

    public static void main(String[] args) {

        LogManager.record("Старт программы");

        File sourceFile1 = new File("src/file1.txt");
        File sourceFile2 = new File("src/file2.txt");
        File destFile1 = new File("src/copy1.txt");
        File destFile2 = new File("src/copy2.txt");

        LogManager.record("Проверка исходных файлов:");
        LogManager.record("  file1.txt существует: " + sourceFile1.exists());
        LogManager.record("  file2.txt существует: " + sourceFile2.exists());

        try {
            // Последовательное копирование
            sequentialCopy(sourceFile1, destFile1, sourceFile2, destFile2);

            if (destFile1.exists()) destFile1.delete();
            if (destFile2.exists()) destFile2.delete();

            parallelCopy(sourceFile1, destFile1, sourceFile2, destFile2);

            LogManager.record("Запуск бинарного анализа скопированных файлов");
            CodeInspector.findProblemZone(destFile1.toPath());
            CodeInspector.findProblemZone(destFile2.toPath());

        } catch (Exception e) {
            LogManager.recordError(e);
            e.printStackTrace();
        }

        LogManager.record("Программа завершена");
    }
}