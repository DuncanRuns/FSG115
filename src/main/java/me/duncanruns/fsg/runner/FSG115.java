package me.duncanruns.fsg.runner;

import me.duncanruns.fsg.cryptography.DRandInfo;
import me.duncanruns.fsg.cryptography.DRandRequester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// The main "driver" class to run the whole filter. Controls seed finding threads and writing to out.txt.

public class FSG115 {

    public static final String VERSION = "1.2.0";

    public static final AtomicBoolean foundSeed = new AtomicBoolean(false);

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

        System.out.println("Getting signature...");
        DRandInfo dRandInfo = new DRandRequester().get("latest");

        List<FSGThread> threads = new ArrayList<>();

        for (int i = 0; i < totalThreads; i++) {
            threads.add(new FSGThread(true, i, dRandInfo));
        }
        for (FSGThread thread : threads) {
            thread.start();
        }

        while (!foundSeed.get()) {
            Thread.sleep(100);
        }

        FilterResult filterResult = null;

        for (FSGThread thread : threads) {
            // I'm pretty damn sure that this is fine in the context. Every reference that can be accessed by multiple
            // threads is either volatile or atomic; anything being handled by the thread at the time of a .stop should
            // be a part of itself and shouldn't affect anything in other threads.
            if (thread.isAlive()) thread.stop();
            if (filterResult == null && thread.getFilterResult() != null) filterResult = thread.getFilterResult();
        }

        assert filterResult != null;
        System.out.println("\nSeed:\n" + filterResult.getWorldSeed() + "\n");
        System.out.println("Token:\n" + filterResult.toToken());

        if (args.length > 1 && args[1].equals("write")) {
            writeToFile(String.valueOf(filterResult.getWorldSeed()), filterResult.toToken());
        }
    }

    private static void writeToFile(String seed, String token) throws IOException {
        File file = new File("out.txt");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(seed + "\n" + token);
        fileWriter.close();
    }
}
