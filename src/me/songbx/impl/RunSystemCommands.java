package me.songbx.impl;

import me.songbx.util.MyThreadCount;

import java.io.*;

public class RunSystemCommands extends Thread{
    private String command;
//    private String outPut;
    private MyThreadCount threadCount;

    public RunSystemCommands(String command, MyThreadCount threadCount){
        this.command=command;
        this.threadCount=threadCount;
//        this.outPut = outPut;
    }
    public void run(){
        try {
//            System.out.println("15 " + command + " 15");
            Runtime r = Runtime.getRuntime();
            String [] cmd = {"sh", "-c", command};
            Process p = r.exec(cmd);
            BufferedInputStream is = new BufferedInputStream(p.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            r.maxMemory();
            p.waitFor();
//            if (p.exitValue() != 0) {
//                System.err.println("there is something wrong with " + command);
//            }

//            PrintWriter printWriter = new PrintWriter(outPut);
//            String s = null;
//            while ((s = reader.readLine()) != null) {
//                printWriter.println(s);
//            }
//            printWriter .close();
            p.destroy();
//            System.out.println( command + " done");
        }catch(Exception e1) {
            e1.getStackTrace();
        }finally {
            threadCount.countDown();
        }
    }
}
