package io.helidon.examples.quickstart.se;

import java.util.ArrayList;

public class DatabaseResult {

        private ArrayList<String> lines = new ArrayList<String>();
        private int numColumnsListArray;
        private int linesLength;

        private String[][] selectLine;
        private int numColumnsSelectLine;
        private int selectLineLength;




    public DatabaseResult() {
    }

    public DatabaseResult(ArrayList<String> lines, String[][] selectLine, int numColumnsSelectLine, int numColumnsListArray, int linesLength, int selectLineLength) {
        this.lines = lines;
        this.selectLine = selectLine;
        this.numColumnsSelectLine = numColumnsSelectLine;
        this.numColumnsListArray = numColumnsListArray;
        this.linesLength = linesLength;
        this.selectLineLength = selectLineLength;
    }

    public String[][] getSelectLine() {
        return selectLine;
    }

    public int getNumColumnsSelectLine() {
        return numColumnsSelectLine;
    }

    public int getSelectLineLength() {
        return selectLineLength;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    public int getNumColumnsListArray() {
        return numColumnsListArray;
    }

    public int getLinesLength() {
        return linesLength;
    }
}
