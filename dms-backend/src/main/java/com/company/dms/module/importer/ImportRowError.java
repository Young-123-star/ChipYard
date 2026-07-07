package com.company.dms.module.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRowError {
    private int rowNumber;
    private String field;
    private String value;
    private String message;
}
