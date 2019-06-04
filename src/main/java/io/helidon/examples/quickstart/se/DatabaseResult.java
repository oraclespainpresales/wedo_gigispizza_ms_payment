package io.helidon.examples.quickstart.se;

import java.util.ArrayList;

public class DatabaseResult {
        private ArrayList<String> lines;
        private int numColumnsListArray;
        private int linesLength;

        private String[][] selectLine;
        private int numColumnsSelectLine;
        private int selectLineLength;

        public DatabaseResult() {
            this.lines                = new ArrayList<>();
            this.selectLine           = null;
            this.numColumnsListArray  = 0;
            this.linesLength          = 0;
            this.numColumnsSelectLine = 0;
            this.selectLineLength     = 0;
        }

        public DatabaseResult(ArrayList<String> lines,
                              String[][] selectLine,
                              int numColumnsSelectLine,
                              int numColumnsListArray,
                              int linesLength,
                              int selectLineLength) {

            this.lines                = lines;
            this.selectLine           = selectLine;
            this.numColumnsSelectLine = numColumnsSelectLine;
            this.numColumnsListArray  = numColumnsListArray;
            this.linesLength          = linesLength;
            this.selectLineLength     = selectLineLength;
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
