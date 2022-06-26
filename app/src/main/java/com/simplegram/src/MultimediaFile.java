package com.simplegram.src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * A 'MultimediaFile' class that contains all required information
 * for a multimedia file message in a topic.
 *
 * Each chunk is represented as a byte array and all the chunks required to
 * rebuild the multimedia file are stored in an arraylist data structure.
 */
public class MultimediaFile extends Value implements Serializable {


    private String filename;
    private int fileSize;
    private ArrayList<byte[]> chunks;
    private String type;

    public MultimediaFile(
            String sentFrom,
            String filename,
            int fileSize,
            ArrayList<byte[]> chunks,
            String type
    ) {
        super(sentFrom);
        this.filename = filename;
        this.fileSize = fileSize;
        this.chunks = chunks;
        this.fileSize = fileSize;
        this.type = type;
    }


    public MultimediaFile(LocalDateTime dateSent, String sentFrom, String filename, int fileSize, ArrayList<byte[]> chunks, String type) {
        super(dateSent, sentFrom);
        this.filename = filename;
        this.fileSize = fileSize;
        this.chunks = chunks;
        this.fileSize = fileSize;
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public int getFileSize() {
        return fileSize;
    }

    public ArrayList<byte[]> getChunks() {
        return chunks;
    }

    public String getType(){
        return this.type;
    }

    @Override
    public String toString() {
        return this.filename;
    }
}