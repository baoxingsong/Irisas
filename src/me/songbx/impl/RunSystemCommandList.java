package me.songbx.impl;

import me.songbx.util.MyThreadCount;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class RunSystemCommandList extends Thread{
    private String[] command ;
    private MyThreadCount threadCount;
    private String outPutFile;

    public RunSystemCommandList(String[] command, MyThreadCount threadCount){
        this.command=command;
        this.threadCount=threadCount;
    }
    public RunSystemCommandList(String[] command, MyThreadCount threadCount, String outPutFile){
        this.command=command;
        this.threadCount=threadCount;
        this.outPutFile=outPutFile;
    }
    public void run(){
        try {
//            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder.redirectErrorStream(true);
//            Process p = processBuilder.start();

            Runtime r = Runtime.getRuntime();
            Process p = r.exec(command);

            BufferedInputStream is = new BufferedInputStream(p.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            BufferedInputStream is2 = new BufferedInputStream(p.getErrorStream());
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));

            p.waitFor();
//            if (p.exitValue() != 0) {
//                System.out.println("there is something wrong with " + r.toString());
//            }
            String s = null;
            PrintWriter printWriter = new PrintWriter(outPutFile);
            while ((s = reader.readLine()) != null) {
                printWriter.println(s);
            }
            printWriter .close();

            while ((s = reader2.readLine()) != null) {
                System.out.println(s);
            }

        }catch(Exception e1) {
            e1.getStackTrace();
        }finally {
            threadCount.countDown();
        }
    }
}
