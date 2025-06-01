package commonlib.models;

import java.io.Serializable;

public class FileRequest implements Serializable {
    public enum OperationType {
        ADD, MODIFY, DELETE
    }

    private String filename;
    private String department;
    private OperationType operation;
    private Token token;
    private byte[] content;


    public FileRequest(String filename, String department, OperationType operation, Token token, byte[] content) {
        this.filename = filename;
        this.department = department;
        this.operation = operation;
        this.token = token;
        this.content = content;
    }


    public FileRequest(String filename, String department, OperationType operation, Token token) {
        this(filename, department, operation, token, null);
    }

    public String getFilename() {
        return filename;
    }

    public String getDepartment() {
        return department;
    }

    public OperationType getOperation() {
        return operation;
    }

    public Token getToken() {
        return token;
    }

    public byte[] getContent() {
        return content;
    }
}
