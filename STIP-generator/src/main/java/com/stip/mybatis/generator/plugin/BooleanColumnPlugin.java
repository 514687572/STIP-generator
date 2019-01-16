package com.stip.mybatis.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

/**
 * 
 * 将数据库中 is_XXXX 开头的字段，并且长度为1，做如下处理 1.字段类型改为 boolean，字段为 XXXX，
 * 
 * @author cja
 *
 */
public class BooleanColumnPlugin extends PluginAdapter {

    private static final String PREFIX_STRING = "is_";

    private static final String PREFIX_STRING_IS = "is";

    private static final String JAVA_TTYPE_BOOLEAN = "Boolean";
    
	enum KeyWords {
		Keyabstract("abstract"), 
		Keyassert("assert"), 
		Keycontinue("continue"), 
		Keydefault("default"),
		Keydouble("double"), 
		Keyelse("else"), 
		Keyenum("enum"), 
		Keyextends("extends"), 
		Keyfinal("final"),
		Keyfinally("finally"), 
		Keyfloat("float"), 
		Keyfor("for"), 
		Keygoto("goto"), 
		Keyif("if"),
		Keyimplements("implements"), 
		Keyimport("import"), 
		Keyinstanceof("instanceof"), 
		Keyint("int"),
		Keyinterface("interface"), 
		Keylong("long"), 
		Keynative("native"), 
		Keynew("new"), 
		Keypackage("package"),
		Keyprivate("private"), 
		Keyprotected("protected"), 
		Keypublic("public"), 
		Keyreturn("return"), 
		Keyshort("short"),
		Keystatic("static"), 
		Keystrictfp("strictfp"), 
		Keysuper("super"), 
		Keyswitch("switch"),
		Keysynchronized("synchronized"), 
		Keythis("this"), 
		Keythrow("throw"), 
		Keythrows("throws"),
		Keytransient("transient"), 
		Keytry("try"), 
		Keyvoid("void"), 
		Keyvolatile("volatile"), 
		Keywhile("while");

		private String keyType;

		private KeyWords(String keyType) {
			this.keyType = keyType;
		}
	}

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            int length = introspectedColumn.getLength();
            String columnName = introspectedColumn.getActualColumnName();

            if (columnName.startsWith(PREFIX_STRING) && length == 1) {
                String propertyName = introspectedColumn.getJavaProperty().substring(PREFIX_STRING_IS.length());

                introspectedColumn.setJavaProperty(toLowerCaseFirstOne(propertyName));
                introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(JAVA_TTYPE_BOOLEAN));
            }
            
            try {
				if(KeyWords.valueOf("Key"+introspectedColumn.getJavaProperty())!=null) {
					introspectedColumn.setJavaProperty(toLowerCaseFirstOne(introspectedColumn.getJavaProperty()+"ext"));
				}
			} catch (Exception e) {
				
			}
            
        }
    }

    private String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

}
