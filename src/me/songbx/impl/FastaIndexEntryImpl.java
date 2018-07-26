package me.songbx.impl;

import me.songbx.model.ChromoSome;
import me.songbx.model.FastaIndexEntry;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastaIndexEntryImpl {
    private ArrayList<String> names = new ArrayList<String>(); // the vector could keep the order
    private HashMap<String, FastaIndexEntry> entries = new HashMap<String, FastaIndexEntry>();
    public synchronized void readFastaIndexFile( String fastaIndexFileLocation ){
        File file = new File(fastaIndexFileLocation);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            Pattern p1 = Pattern.compile("^(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)");
            StringBuffer sequenceBuffer = new StringBuffer();
            while ((tempString = reader.readLine()) != null) {
                Matcher m1 = p1.matcher(tempString);
                if (m1.find()) {
                    FastaIndexEntry fastaIndexEntry = new FastaIndexEntry(m1.group(1),  Integer.parseInt(m1.group(2)), Long.parseLong(m1.group(3)), Integer.parseInt(m1.group(4)), Integer.parseInt(m1.group(5)));
                    this.entries.put(m1.group(1), fastaIndexEntry);
                    //fastaIndexEntry.outPut();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
    }

    public synchronized void createFastaIndexFile( String fastaFileLocation ){
        long offset = 0; // bytes offset of sequence from start of file
        FastaIndexEntry entry = new FastaIndexEntry();
        int line_length=0;
        String name="";
        File file = new File(fastaFileLocation);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            Pattern p1 = Pattern.compile("^>(\\S+)");
            StringBuffer sequenceBuffer = new StringBuffer();

            while ((tempString = reader.readLine()) != null) {
                line_length = tempString.length();
                Matcher m1 = p1.matcher(tempString);
                if (m1.find()) {
                    if( names.size() > 0 ){
                        entry.setName(name);
                        entries.put(name, new FastaIndexEntry(entry));
                        entry.setLength(0);
                        entry.setOffset(-1);
                        //entries.get(name).outPut();
                    }
                    name = m1.group(1);
                    names.add(name);
                } else {
                    if( -1 == entry.getOffset() ) {
                        entry.setOffset(offset);
                    }
                    entry.setLength(entry.getLength() + line_length);
                    if( entry.getLine_len() == 0 ){
                        entry.setLine_len(line_length+1); // this if for the first line of the current fasta entry
                        entry.setLine_blen(tempString.trim().length());
                    }
                }
                offset += line_length + 1;
            }

            reader.close();
            entry.setName(name);
            entries.put(name, new FastaIndexEntry(entry));
            //entries.get(name).outPut();

            PrintWriter outPut = new PrintWriter(fastaFileLocation + ".fai");
            for(String nameI : names ) {
                outPut.println( nameI + "\t" + entries.get(nameI).getLength() + "\t" + entries.get(nameI).getOffset()+"\t"+ entries.get(nameI).getLine_blen()+"\t"+entries.get(nameI).getLine_len());
            }
            outPut.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.getStackTrace();
                }
            }
        }
    }

    public synchronized ArrayList<String> getNames() {
        return names;
    }

    public synchronized void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public synchronized HashMap<String, FastaIndexEntry> getEntries() {
        return entries;
    }

    public synchronized void setEntries(HashMap<String, FastaIndexEntry> entries) {
        this.entries = entries;
    }

    public static void main(String[] argv ){
        new FastaIndexEntryImpl().readFastaIndexFile("/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/PA9998.fa.fai");
        new FastaIndexEntryImpl().createFastaIndexFile("/biodata/dep_tsiantis/grp_gan/song/rdINDELallHere/inputData/fullSdiFile/PA9998.fa");
    }
}
