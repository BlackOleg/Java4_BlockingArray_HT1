package ru.olegivanov;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);
    public static Thread textGenerator;

    public static void main(String[] args) throws InterruptedException {
        textGenerator = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                String text = generateText("abc", 100000);
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        textGenerator.start();

        Thread a = getThread(queueA, 'a');
        Thread b = getThread(queueB, 'b');
        Thread c = getThread(queueC, 'c');
        a.start();
        b.start();
        c.start();

        a.join();
        b.join();
        c.join();

        System.out.println("READY - JAVA4.4 - HT1");
    }

    private static Thread getThread(BlockingQueue<String> queue, char letter) {
        return new Thread(() -> {
            int max = findMax(queue, letter);
            System.out.println("Max количество буквы " + letter + " = " + max);
        });
    }

    private static int findMax(BlockingQueue<String> queue, char letter) {
        int count = 0;
        int max = 0;
        String text;
        try {
            while (textGenerator.isAlive()) {
                text = queue.take();
                for (char c : text.toCharArray()) {
                    if (c == letter) count++;
                }
                if (count > max) max = count;
                count = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " был прерван");
            return -1;
        }
        return max;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}