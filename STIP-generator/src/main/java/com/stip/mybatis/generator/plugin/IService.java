package com.stip.mybatis.generator.plugin;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface IService<T extends BaseModel<PK>, TE extends BaseModelExample, PK extends Serializable> {

	int countByExample(TE example);
	
	int deleteByExample(TE example);

	int deleteByPrimaryKey(PK id);

	int insert(T record);

	int insertSelective(T record);

	List<T> selectByExample(TE example);

	T selectByPrimaryKey(PK id);

	int updateByExampleSelective(@Param("record") T record, @Param("example") TE example);

	int updateByExample(@Param("record") T record, @Param("example") TE example);

	int updateByPrimaryKeySelective(T record);

	int updateByPrimaryKey(T record);
	
	int batchInsert(List<T> records);

}
