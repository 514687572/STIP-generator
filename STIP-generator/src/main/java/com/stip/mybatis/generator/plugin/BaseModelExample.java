package com.stip.mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseModelExample extends AbstractExample {
	protected String orderByClause;
	protected String groupByClause;
	protected String havingClause;
	protected boolean distinct;
	protected List<Criteria> oredCriteria;

	public BaseModelExample() {
		oredCriteria = new ArrayList<Criteria>();
	}

	protected abstract static class GeneratedCriteria {
		protected List<Criterion> criteria;
	}

	public static class Criterion {
		private String condition;
		private Object value;
		private Object secondValue;
		private boolean noValue;
		private boolean singleValue;
		private boolean betweenValue;
		private boolean listValue;

		protected Criterion(String condition) {
			this.condition = condition;
			this.noValue = true;
		}

		public String getCondition() {
			return condition;
		}

		public Object getValue() {
			return value;
		}

		public Object getSecondValue() {
			return secondValue;
		}

		public boolean isNoValue() {
			return noValue;
		}

		public boolean isSingleValue() {
			return singleValue;
		}

		public boolean isBetweenValue() {
			return betweenValue;
		}

		public boolean isListValue() {
			return listValue;
		}
	}

	protected Criteria createCriteriaInternal() {
		return new Criteria();
	}

	public void or(Criteria criteria) {
		oredCriteria.add(criteria);
	}

	public Criteria createCriteria() {
		Criteria criteria = createCriteriaInternal();
		if (oredCriteria.size() == 0) {
			oredCriteria.add(criteria);
		}
		return criteria;
	}

	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public void setGroupByClause(String groupByClause) {
		this.groupByClause = groupByClause;
	}

	public String getGroupByClause() {
		return groupByClause;
	}

	public void setHavingClause(String havingClause) {
		this.havingClause = havingClause;
	}

	public String getHavingClause() {
		return havingClause;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public static class Criteria {
		protected List<Criterion> criteria;

		protected Criteria() {
			super();
			criteria = new ArrayList<Criterion>();
		}

		public List<Criterion> getCriteria() {
			return criteria;
		}

		protected void addCriterion(String condition) {
			criteria.add(new Criterion(condition));
		}
	}
}
