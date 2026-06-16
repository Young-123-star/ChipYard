package com.company.dms.module.fee.vo;

import lombok.Data;

@Data
public class GenerateResultVO {
    private int generated;
    private int skipped;

    public static GenerateResultVO of(int generated, int skipped) {
        GenerateResultVO vo = new GenerateResultVO();
        vo.setGenerated(generated);
        vo.setSkipped(skipped);
        return vo;
    }
}
