import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class FileCopy {


    private static void copyFileUsingNIO(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void posledCopy(File source1, File dest1, File source2, File dest2) throws IOException {
        System.out.println("Начало последовательного копирования:");

        long startTime = System.nanoTime();

        copyFileUsingNIO(source1, dest1);
        System.out.println("Первый файл скопирован: " + source1.getName() + " -> " + dest1.getName());

        copyFileUsingNIO(source2, dest2);
        System.out.println("Второй файл скопирован: " + source2.getName() + " -> " + dest2.getName());

        long endTime = System.nanoTime();
        long time = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Время последовательного копирования: " + time + " мс\n");
    }

    private static void parallelCopy(File source1, File dest1, File source2, File dest2) throws InterruptedException {
        System.out.println("Начало параллельного копирования:");

        long startTime = System.nanoTime();

        Thread thread1 = new Thread(() -> {
            try {
                copyFileUsingNIO(source1, dest1);
                System.out.println("Первый файл скопирован в потоке: " + source1.getName() + " -> " + dest1.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                copyFileUsingNIO(source2, dest2);
                System.out.println("Второй файл скопирован в потоке: " + source2.getName() + " -> " + dest2.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Время параллельного копирования: " + duration + " мс\n");
    }

    public static void main(String[] args) {
        File sourceFile1 = new File("src/file1.txt");
        File sourceFile2 = new File("src/file2.txt");
        File destFile1 = new File("src/copy1.txt");
        File destFile2 = new File("src/copy2.txt");

        try {
            posledCopy(sourceFile1, destFile1, sourceFile2, destFile2);

            if (destFile1.exists()) destFile1.delete();
            if (destFile2.exists()) destFile2.delete();

            parallelCopy(sourceFile1, destFile1, sourceFile2, destFile2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
