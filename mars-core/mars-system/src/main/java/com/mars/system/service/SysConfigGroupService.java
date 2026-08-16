package com.mars.system.service;

import com.mars.system.entity.SysConfigGroup;
import java.util.List;

/**
 * 系统配置分组 Service
 */
public interface SysConfigGroupService {
    
    /**
     * 获取所有配置分组
     */
    List<SysConfigGroup> listAll();
    
    /**
     * 根据分组编码获取配置
     */
    SysConfigGroup getByGroupCode(String groupCode);
    
    /**
     * 保存配置
     */
    void saveConfig(String groupCode, String configValue);

    /**
     * 保存配置，不存在时自动创建配置分组
     */
    void saveOrCreateConfig(String groupCode, String groupName, String configValue, Integer sort, String remark);
    
    /**
     * 获取配置值
     */
    String getConfigValue(String groupCode, String key);
    
    /**
     * 刷新缓存
     */
    void refreshCache();
}
