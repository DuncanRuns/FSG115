package me.duncanruns.fsg.runner;

import me.duncanruns.fsg.cryptography.DRandInfo;
import me.duncanruns.fsg.cryptography.DRandRequester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// The main "driver" class to run the whole filter. Controls seed finding threads and writing to out.txt.

public class FSG115 {

    public static final String VERSION = FSG115.class.getPackage().getImplementationVersion() == null ? "DEV" : FSG115.class.getPackage().getImplementationVersion();

    public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException {
        int totalThreads;
        if (args.length > 0) {
            totalThreads = Integer.parseInt(args[0]);
        } else {
            totalThreads = 4;
        }

        System.out.println("--------------------");
        System.out.println("MC 1.15 FSG v" + VERSION + " by DuncanRuns");
        System.out.println("Running " + totalThreads + " threads on Java " + System.getProperty("java.version"));
        System.out.println("--------------------");

        FilterResult result = findSeed(totalThreads, true);

        System.out.println("\nSeed:\n" + result.getWorldSeed() + "\n");
        System.out.println("Token:\n" + result.toToken());

        if (args.length > 1 && args[1].equals("write")) {
            writeToFile(String.valueOf(result.getWorldSeed()), result.toToken());
        }
    }

    public static FilterResult findSeed(int totalThreads) throws IOException, NoSuchAlgorithmException, InterruptedException {
        return findSeed(totalThreads, false);
    }

    public static FilterResult findSeed(int totalThreads, boolean printProgress) throws IOException, NoSuchAlgorithmException, InterruptedException {
        final DRandInfo dRandInfo = new DRandRequester().get("latest");
        final Instant now = Instant.now();
        final Object LOCK = new Object();
        final AtomicBoolean found = new AtomicBoolean(false);
        final AtomicReference<FilterResult> resultRef = new AtomicReference<>(null);

        final Consumer<FilterResult> resultConsumer = filterResult -> {
            synchronized (LOCK) {
                if (found.get()) {
                    return;
                }
                found.set(true);
                resultRef.set(filterResult);
            }
        };
        final BooleanSupplier continueSupplier = () -> !found.get();

        List<FSGThread> threads = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            threads.add(new FSGThread(printProgress, i, dRandInfo, now, resultConsumer, continueSupplier));
        }
        for (FSGThread thread : threads) {
            thread.start();
        }

        while (continueSupplier.getAsBoolean()) {
            Thread.sleep(10);
        }

        return resultRef.get();
    }

    private static void writeToFile(String seed, String token) throws IOException {
        File file = new File("out.txt");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(seed + "\n" + token);
        fileWriter.close();
    }
}
