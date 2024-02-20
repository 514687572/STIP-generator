package com.stip.mybatis.generator.plugin;

import java.io.Serializable;

/**
 * all Model's base calss
 * 
 * @author chenjunan
 *
 */
public abstract class BaseModel<PK extends Serializable> implements Serializable {

    private static final long serialVersionUID = -6590882888801386323L;

    protected PK sid;

    public PK getSid() {
        return sid;
    }

    public void setSid(PK sid) {
        this.sid = sid;
    }
	
}
