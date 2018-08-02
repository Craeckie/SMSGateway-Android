package de.sanemind.smsgateway.model;

import java.util.ArrayList;

public class Buttons extends ArrayList<Buttons.Row> {
    public Row addRow(ArrayList<String> items) {
        Row row = new Row(items);
        add(row);
        return row;
    }

    public class Row extends ArrayList<String> {
        public Row(ArrayList<String> items) {
            super(items);
        }
    }
}
