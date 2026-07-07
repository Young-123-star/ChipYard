package com.company.dms.module.importer;

import com.company.dms.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Data Import")
@RestController
@RequestMapping("/api/import")
public class ImportController {
    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @Operation(summary = "Download import template")
    @GetMapping("/templates/{type}")
    public ResponseEntity<byte[]> template(@PathVariable String type) {
        return xlsx(type + "-template.xlsx", importService.template(type, false));
    }

    @Operation(summary = "Download sample import file")
    @GetMapping("/samples/{type}")
    public ResponseEntity<byte[]> sample(@PathVariable String type) {
        return xlsx(type + "-sample.xlsx", importService.template(type, true));
    }

    @Operation(summary = "Validate xlsx before import")
    @PostMapping(value = "/{type}/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportResult> validate(@PathVariable String type, @RequestParam("file") MultipartFile file) throws Exception {
        return R.ok(importService.validate(type, file.getBytes()));
    }

    @Operation(summary = "Execute xlsx import")
    @PostMapping(value = "/{type}/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportResult> execute(@PathVariable String type, @RequestParam("file") MultipartFile file) throws Exception {
        return R.ok(importService.execute(type, file.getBytes()));
    }

    private ResponseEntity<byte[]> xlsx(String filename, byte[] body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }
}
