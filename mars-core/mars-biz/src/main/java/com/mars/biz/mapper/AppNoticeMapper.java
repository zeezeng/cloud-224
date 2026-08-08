package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.AppNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * App 首页公告 Mapper
 */
@Mapper
public interface AppNoticeMapper extends BaseMapper<AppNotice> {
}
