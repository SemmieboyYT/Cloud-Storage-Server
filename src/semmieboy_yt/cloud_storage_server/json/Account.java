package semmieboy_yt.cloud_storage_server.json;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class Account {
    public String username;
    public byte[] password;
    public String uuid;
    private MessageDigest sha256;

    public Account() {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
    }

    public Account setup(String username, String password) {
        this.username = username;
        this.password = sha256.digest(password.getBytes());
        this.uuid = UUID.randomUUID().toString();

        return this;
    }

    public boolean validate(String password) {
        return Arrays.equals(sha256.digest(password.getBytes()), this.password);
    }
}
