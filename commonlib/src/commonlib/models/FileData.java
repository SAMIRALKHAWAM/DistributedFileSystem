package commonlib.models;

import java.io.Serializable;

public class FileData implements Serializable {
    private byte[] data;

    public FileData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
