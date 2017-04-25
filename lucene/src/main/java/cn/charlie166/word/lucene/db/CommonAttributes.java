package cn.charlie166.word.lucene.db;

import java.util.Map;

/**
* @ClassName: CommonAttributes 
* @Description: 公共属性类
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月25日 
*
 */
public class CommonAttributes {

	/**建立索引时，标题的键值**/
	public static final String KEY_TITLE = "title";
	
	/**索引保存的主键ID键***/
	public static final String KEY_ID = "id";
	
	/**索引保存，内容的键***/
	public static final String KEY_CONTENT = "content";
	
	/**索引保存位置**/
	public static final String INDEX_STORE = "db_index";
	
	/***
	* @Title: convertMapToString 
	* @Description: 返回MAP的字符串形式
	* @param map
	* @return
	 */
	public static String convertMapToString(Map<String, Object> map){
		if(map != null && !map.isEmpty()){
			StringBuilder str = new StringBuilder();
			str.append("[");
			if(map.containsKey("id")){
				str.append("id:" + map.get("id")).append(";");
				map.remove("id");
			}
			for(String key: map.keySet()){
				str.append(key + ":" + map.get(key).toString()).append(";");
			}
			str.append("]");
			return str.toString();
		}
		return null;
	}
}