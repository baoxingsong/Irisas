package me.songbx.model;

public class FastaIndexEntry {
    private String name; // sequence name
    private int length; // length of sequence
    private long offset; // bytes offset of sequence from start of file
    private int line_blen; // line length in bytes, sequence characters
    private int line_len; // line length including newline
    public FastaIndexEntry(){
        this.name = "";
        this.length = 0;
        this.offset = -1;
        this.line_blen = 0;
        this.line_len = 0;
    }
    public FastaIndexEntry(String _name, int _length, long _offset, int _line_blen, int _line_len){
        this.name = _name;
        this.length = _length;
        this.offset = _offset;
        this.line_blen = _line_blen;
        this.line_len = _line_len;
    }

    public FastaIndexEntry(FastaIndexEntry fastaIndexEntry){
        this.name = fastaIndexEntry.name;
        this.length = fastaIndexEntry.length;
        this.offset = fastaIndexEntry.offset;
        this.line_blen = fastaIndexEntry.line_blen;
        this.line_len = fastaIndexEntry.line_len;
    }


    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized int getLength() {
        return length;
    }

    public synchronized void setLength(int length) {
        this.length = length;
    }

    public synchronized long getOffset() {
        return offset;
    }

    public synchronized void setOffset(long offset) {
        this.offset = offset;
    }

    public synchronized int getLine_blen() {
        return line_blen;
    }

    public synchronized void setLine_blen(int line_blen) {
        this.line_blen = line_blen;
    }

    public synchronized int getLine_len() {
        return line_len;
    }

    public synchronized void setLine_len(int line_len) {
        this.line_len = line_len;
    }

    public synchronized void outPut(){
        System.out.println("name:" + name + " length: " + length + " offset: " + offset + " line_blen: " + line_blen + " line_len: " + line_len);
    }
}
