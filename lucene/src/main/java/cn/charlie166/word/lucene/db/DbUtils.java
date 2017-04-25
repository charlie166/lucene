package cn.charlie166.word.lucene.db;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: DbUtils 
* @Description: 数据库工具类
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月24日 
*
 */
public class DbUtils {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public SqlSession getSession(){
		String resource = "mybatis/mybatis.xml";
		//使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
		InputStream is = DbUtils.class.getClassLoader().getResourceAsStream(resource);
		//构建sqlSession的工厂 
		SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
		SqlSession session = sessionFactory.openSession();
		return session;
	}
	
	/**
	* @Title: dbTest 
	* @Description: 获取通知公告列表数据
	 */
	public List<Map<String, Object>> getAdviceList(){
		String statement = "cn.charlie166.word.lucene.db.mapper.TestMapper.selectAdvice";
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> retList = this.getSession().selectList(statement, map);
		logger.debug("查询结果:" + retList.size());
		return retList;
	}
	
	/**
	* @Title: getDataList 
	* @Description: 查询表数据
	* @param tableName 表名
	* @return
	 */
	public List<Map<String, Object>> getDataList(String tableName){
		if(tableName != null && !"".equals(tableName.trim())){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("table_name", tableName);
			String statement = "cn.charlie166.word.lucene.db.mapper.TestMapper.selectList";
			return this.getSession().selectList(statement, map);
		}
		return Collections.emptyList();
	}
	
	/**
	* @Title: selectById 
	* @Description: 通过主键ID查询表数据
	* @param map id: 主键ID; table_name: 表名;
	* @return
	 */
	public Map<String, Object> selectById(Map<String, Object> map){
		if(map != null && !map.isEmpty()){
			if(map.get("table_name") != null && map.get("id") != null){
				String statement = "cn.charlie166.word.lucene.db.mapper.TestMapper.selectById";
				return this.getSession().selectOne(statement, map);
			}
		}
		return null;
	}
}	