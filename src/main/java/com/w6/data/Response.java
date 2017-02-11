package com.w6.data;

import java.util.List;

public class Response {
    private final List<Word> text;
    private final Table table;

    public Table getTable() {
        return table;
    }

    public Response(final List<Word> text, Table table) {
        this.text = text;
        this.table = table;
    }
}
