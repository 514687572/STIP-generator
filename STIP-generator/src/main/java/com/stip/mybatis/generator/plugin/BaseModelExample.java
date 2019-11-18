package com.stip.mybatis.generator.plugin;

/**
 * all Example's base class and pager methord impl
 * 
 * @author cja
 * 
 */
public class BaseModelExample extends AbstractExample{
	protected Integer pageNum;
	protected Integer records;
	protected Integer fromRowNum;
	protected Integer toRowNum;
	protected String orderByClause;
	protected boolean distinct;
	protected Integer defaultRecords=10;
	
	/**
	 * 
	 * @param pageNum 
	 * @param records one of zhe page records
	 */
	public void setPager(int pageNum,int records){
		if(pageNum<=0||records<0){
			throw new IllegalStateException("pageNum must>0 or records must >0");
		}
		this.setFromRowNum(records * (pageNum - 1));
		this.setToRowNum(records);
	}
	
	/**
	 * default 10 records of one page
	 * @param pageNum 
	 */
	public void setPager(int pageNum){
		if(pageNum<=0){
			throw new IllegalStateException("pageNum must>0");
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
