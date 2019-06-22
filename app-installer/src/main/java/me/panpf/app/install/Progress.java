package me.panpf.app.install;

public class Progress {
    private long totalLength;
    private long completedLength;

    public Progress(long totalLength, long completedLength) {
        this.totalLength = totalLength;
        this.completedLength = completedLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public long getCompletedLength() {
        return completedLength;
    }

    public float getRatio() {
        return totalLength > 0 ? (float) completedLength / (float) totalLength : 0f;
    }
}