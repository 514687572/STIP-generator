package com.stip.mybatis.generator.plugin;

import java.util.ArrayList;
import java.util.List;

public class JoinExample extends BaseExample {
    private List<JoinCriteria> joinCriterias = new ArrayList<>();

    public void addJoin(String joinTable, String joinCondition) {
        joinCriterias.add(new JoinCriteria(joinTable, joinCondition));
    }

    public void addJoin(String joinTable, String joinCondition, String joinType) {
        joinCriterias.add(new JoinCriteria(joinTable, joinCondition, joinType));
    }

    public List<JoinCriteria> getJoinCriterias() {
        return joinCriterias;
    }
}
