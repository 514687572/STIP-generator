package com.stip.mybatis.generator.plugin;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;


/**
 * @author cja
 *
 * base mapper interface
 *
 * @param <T> Model class
 * @param <TE> abstract base example class
 * @param <PK> primary key
 */
public interface GenericMapper<T extends BaseModel<PK>, TE extends AbstractExample, PK extends Serializable> {
	int countByExample(TE example);
	
	int deleteByExample(TE example);

	int deleteByPrimaryKey(PK id);

	int insert(T record);

	int insertSelective(T record);

	List<T> selectByExample(TE example);

	T selectByPrimaryKey(PK id);

	int updateByExampleSelective(@Param("record") T record, @Param("example") TE example);

	int updateByExample(@Param("example") TE example);

	int updateByPrimaryKeySelective(T record);

	int updateByPrimaryKey(T record);
	
	int batchInsert(List<T> records);

}
