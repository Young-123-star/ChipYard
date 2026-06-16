package com.company.dms.module.checkout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.dms.module.checkout.entity.CheckoutOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckoutOrderMapper extends BaseMapper<CheckoutOrder> {
}
