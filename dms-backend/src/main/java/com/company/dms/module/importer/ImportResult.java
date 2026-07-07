package com.company.dms.module.importer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int totalRows;
    private int successRows;
    private List<ImportRowError> errors = new ArrayList<>();

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public void addError(int rowNumber, String field, String value, String message) {
        errors.add(new ImportRowError(rowNumber, field, value, message));
    }
}
