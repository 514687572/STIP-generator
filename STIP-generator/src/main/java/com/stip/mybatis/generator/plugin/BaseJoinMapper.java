package com.stip.mybatis.generator.plugin;

import java.util.List;

public interface BaseJoinMapper<T, E extends JoinExample> {
    List<T> selectJoinByExample(E example);
    
    T selectJoinByPrimaryKey(Object key, E example);
    
    long countJoinByExample(E example);
}
