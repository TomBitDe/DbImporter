package com.home.dbimportermaven.dbtypes;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Database table mata infos.
 */
public class MetaInfoTable {

    private final String tableName;
    private final LinkedList<String> colNames;
    private final HashMap<String, String> colType;
    private final HashMap<String, Integer> displaySize;            // für CHAR
    private final HashMap<String, Integer> numbers;                // für NUMBER - Vorkommastellen
    private final HashMap<String, Integer> decimals;               // für NUMBER - Nachkommastellen

    public MetaInfoTable(String tableName,
                         LinkedList<String> colNames,
                         HashMap<String, String> colType,
                         HashMap<String, Integer> displaySize,
                         HashMap<String, Integer> numbers,
                         HashMap<String, Integer> decimals) {

        this.tableName = tableName;
        this.colNames = colNames;
        this.colType = colType;
        this.displaySize = displaySize;
        this.numbers = numbers;
        this.decimals = decimals;
    }

    public LinkedList<String> getColNames() {
        return colNames;
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return this.colNames.size();
    }

    public String getColtype(String colName) {
        return this.colType.get(colName);
    }

    public int getDisplaySize(String colName) {
        return this.displaySize.get(colName);
    }

    public int getNumbers(String colName) {
        return this.numbers.get(colName);
    }

    public int getDecimals(String colName) {
        return this.decimals.get(colName);
    }
}
