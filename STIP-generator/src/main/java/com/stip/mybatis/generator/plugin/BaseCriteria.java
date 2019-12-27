package com.stip.mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author cja
 *
 */
public abstract class BaseCriteria extends Criterion {
    protected List<Criterion> criteria;

    protected BaseCriteria() {
        super();
        criteria = new ArrayList<Criterion>();
    }

    public boolean isValid() {
        return criteria.size() > 0;
    }

    public List<Criterion> getAllCriteria() {
        return criteria;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    protected BaseExample.Criteria addCriterion(String condition) {
        if (condition == null) {
            throw new RuntimeException("Value for condition cannot be null");
        }
        criteria.add(new Criterion(condition));

        return (BaseExample.Criteria) this;
    }

    protected BaseExample.Criteria addCriterion(String condition, Object value) {
        criteria.add(new Criterion(condition, value));

        return (BaseExample.Criteria) this;
    }

    protected BaseExample.Criteria addCriterion(String condition, Object value, String property) {
        if (value == null) {
            throw new RuntimeException("Value for " + property + " cannot be null");
        }
        criteria.add(new Criterion(condition, value));

        return (BaseExample.Criteria) this;
    }

    protected BaseExample.Criteria addCriterion(String condition, Object value1, Object value2, String property) {
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + property + " cannot be null");
        }
        criteria.add(new Criterion(condition, value1, value2));

        return (BaseExample.Criteria) this;
    }
}