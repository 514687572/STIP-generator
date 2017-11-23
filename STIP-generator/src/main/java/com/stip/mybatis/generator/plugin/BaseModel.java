package com.stip.mybatis.generator.plugin;

import java.io.Serializable;

/**
 * 所有Example的基类，包括分页属性
 * 
 * @author cja
 *
 */
public class BaseModel<PK extends Serializable> implements Serializable {

    private static final long serialVersionUID = -6590882888801386323L;

    protected PK sid;

    public PK getSid() {
        return sid;
    }

    public void setSid(PK sid) {
        this.sid = sid;
    }
	
}
