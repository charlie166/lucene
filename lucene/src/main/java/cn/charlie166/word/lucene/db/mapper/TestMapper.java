package cn.charlie166.word.lucene.db.mapper;

import java.util.List;
import java.util.Map;

/**
* @ClassName: TestMapper 
* @Description:
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月25日 
*
 */
public interface TestMapper {

	/**
	* @Title: selectAdvice 
	* @Description: 查询通知公告
	* @param map 查询条件
	* @return 通知公告数据
	 */
	public List<Map<String, Object>> selectAdvice(Map<String, Object> map);
	
	/**
	* @Title: selectList 
	* @Description: 查询列表数据
	* @param map table_name: 表名,必需
	* @return
	 */
	public List<Map<String, Object>> selectList(Map<String, Object> map);
	
	/**
	* @Title: selectById 
	* @Description: 通过主键ID查询数据
	* @param param 查询条件	table_name: 表名，必需;	id: 主键ID，必需
	* @return
	 */
	public Map<String, Object> selectById(Map<String, Object> param);
}	