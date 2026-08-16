UPDATE `sys_menu`
SET `icon` = 'HelpOutline',
    `update_time` = NOW()
WHERE `permission` = 'yun:feedback:list'
  AND `type` = 2
  AND (`icon` IS NULL OR `icon` = '' OR `icon` = 'ChatbubbleOutline' OR `icon` = 'ChatbubblesOutline');
