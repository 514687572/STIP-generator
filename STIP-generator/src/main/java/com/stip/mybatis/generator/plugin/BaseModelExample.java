package com.stip.mybatis.generator.plugin;

/**
 * 所有Example的基类，包括分页属性
 * 
 * @author cja
 * 
 */
public class BaseModelExample {
	protected Integer pageNum;
	protected Integer records;
	protected Integer fromRowNum;
	protected Integer toRowNum;
	protected String orderByClause;
	protected boolean distinct;
	protected Integer defaultRecords=10;
	
	/**
	 * 
	 * @param pageNum 第几页
	 * @param records 每页显示行数
	 */
	public void setPager(int pageNum,int records){
		if(pageNum<=0||records<0){
			throw new IllegalStateException("页数需要大于0或记录数不能为负数");
		}
		this.setFromRowNum(records * (pageNum - 1));
		this.setToRowNum(records);
	}
	
	/**
	 * 默认每页显示10条记录
	 * @param pageNum 页码
	 */
	public void setPager(int pageNum){
		if(pageNum<=0){
			throw new IllegalStateException("页数需要大于0");
		}
		this.setFromRowNum(defaultRecords * (pageNum - 1));
		this.setToRowNum(defaultRecords);
	}

	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void clear() {
		orderByClause = null;
		distinct = false;
	}

	public Integer getFromRowNum() {
		return fromRowNum;
	}

	public void setFromRowNum(Integer fromRowNum) {
		this.fromRowNum = fromRowNum;
	}

	public Integer getToRowNum() {
		return toRowNum;
	}

	public void setToRowNum(Integer toRowNum) {
		this.toRowNum = toRowNum;
	}

}
