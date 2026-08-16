package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.AppFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * App 用户反馈 Mapper
 */
@Mapper
public interface AppFeedbackMapper extends BaseMapper<AppFeedback> {
}
