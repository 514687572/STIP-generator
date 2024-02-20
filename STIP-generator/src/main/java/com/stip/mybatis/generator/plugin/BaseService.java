
package com.stip.mybatis.generator.plugin;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * service抽象类
 * @author chenjunan
 *
 * @param <T> 实体类
 * @param <TE>对应example
 * @param <PK>主键类型
 */
public abstract class BaseService<T extends BaseModel<PK>, TE extends AbstractExample, PK extends Serializable> implements IService<T, TE, PK>{
    @Autowired
    protected GenericMapper<T, TE, PK> dao;

	public int countByExample(TE example) {
		return dao.countByExample(example);
	}
	
	public int deleteByExample(TE example) {
		return dao.deleteByExample(example);
	}

	public int deleteByPrimaryKey(PK id) {
		return dao.deleteByPrimaryKey(id);
	}

	public int insert(T record) {
		return dao.insert(record);
	}

	public int insertSelective(T record) {
		return dao.insertSelective(record);
	}

	public List<T> selectByExample(TE example) {
		return dao.selectByExample(example);
	}

	public T selectByPrimaryKey(PK id) {
		return dao.selectByPrimaryKey(id);
	}

	public int updateByExampleSelective(@Param("record") T record, @Param("example") TE example) {
		return dao.updateByExampleSelective(record, example);
	}

	public int updateByExample(@Param("example") TE example) {
		return dao.updateByExample(example);
	}

	public int updateByPrimaryKeySelective(T record) {
		return dao.updateByPrimaryKeySelective(record);
	}

	public int updateByPrimaryKey(T record) {
		return dao.updateByPrimaryKey(record);
	}
	
	public int batchInsert(List<T> records) {
		return dao.batchInsert(records);
	}

}
