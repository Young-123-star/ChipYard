package com.company.dms.module.resident.vo;

import com.company.dms.module.resident.entity.Resident;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/** 居住人视图对象：身份证号、手机号脱敏后返回前端，其余字段与实体一致。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResidentVO extends Resident {

    public static ResidentVO of(Resident r) {
        if (r == null) return null;
        ResidentVO vo = new ResidentVO();
        BeanUtils.copyProperties(r, vo);
        vo.setIdCard(mask(r.getIdCard(), 6, 4));
        vo.setPhone(mask(r.getPhone(), 3, 4));
        return vo;
    }

    /** 脱敏：保留前 head 位与后 tail 位，中间以 * 代替；长度不足时全部掩码。 */
    private static String mask(String value, int head, int tail) {
        if (value == null || value.isEmpty()) return value;
        if (value.length() <= head + tail) return "*".repeat(value.length());
        return value.substring(0, head) + "*".repeat(value.length() - head - tail) + value.substring(value.length() - tail);
    }
}
