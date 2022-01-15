package org.texastorque.util.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CSVReader<T> {

    private String fileName;
    private BufferedReader reader;
    private RowTransform<T> mapper;

    public CSVReader(String fileName, RowTransform<T> mapper) {
        this.fileName = fileName;
        this.mapper = mapper;
    }

    public static CSVReader<String[]> getDefaultReader(String fileName) {
        return new CSVReader<>(fileName, new StringTransform());
    }

    public void readFile() {
        try {
            reader = new BufferedReader(new FileReader(fileName));

            String line = "";
            while ((line = reader.readLine()) != null) {
                T result = mapper.parseRow(line.split(","));
            }
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface RowTransform<T> {
        T parseRow(String[] row);
    }

    public static class StringTransform implements RowTransform<String[]> {

        @Override
        public String[] parseRow(String[] row) {
            return row;
        }

    } 
}

