package OtherFunctions.ReSdiWithWindowsedMsa;

import java.util.Objects;

public class MsaFile implements Comparable<MsaFile>{
    private int start;
    private int end;
    private String filePath;

    public MsaFile(int start, int end, String filePath) {
        this.start = start;
        this.end = end;
        this.filePath = filePath;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsaFile msaFile = (MsaFile) o;
        return Objects.equals(filePath, msaFile.filePath);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(filePath);
    }

    public synchronized int compareTo(MsaFile msaFile){
        return (this.start - msaFile.getStart());
    }

    public synchronized int getStart() {
        return start;
    }

    public synchronized void setStart(int start) {
        this.start = start;
    }

    public synchronized int getEnd() {
        return end;
    }

    public synchronized void setEnd(int end) {
        this.end = end;
    }

    public synchronized String getFilePath() {
        return filePath;
    }

    public synchronized void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
