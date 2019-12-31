package com.stip.mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * all Example's base class and pager methord impl
 *
 * @author cja
 */
public class BaseExample extends AbstractExample{
    protected Integer pageNum;
    protected Integer records;
    protected Integer fromRowNum;
    protected Integer toRowNum;
    protected String orderByClause;
    protected boolean distinct;
    protected Integer defaultRecords = 10;

    protected List<Criteria> oredCriteria;

    public BaseExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    protected abstract static class GeneratedCriteria extends BaseCriteria {
        public BaseCriteria addCriterion(String condition) {
            return super.addCriterion(condition);
        }

        public BaseCriteria addCriterion(String condition, Object value) {
            return super.addCriterion(condition, value);
        }

        public BaseCriteria addCriterion(String condition, Object value, String property) {
            return super.addCriterion(condition, value, property);
        }

        public BaseCriteria addCriterion(String condition, Object value1, Object value2, String property) {
            return super.addCriterion(condition, value1, property);
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * @param pageNum
     * @param records one of zhe page records
     */
    public void setPager(int pageNum, int records) {
        if (pageNum <= 0 || records < 0) {
            throw new IllegalStateException("pageNum must>0 or records must >0");
        }
        this.setFromRowNum(records * (pageNum - 1));
        this.setToRowNum(records);
    }

    /**
     * default 10 records of one page
     *
     * @param pageNum
     */
    public void setPager(int pageNum) {
        if (pageNum <= 0) {
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
