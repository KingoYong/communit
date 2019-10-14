package com.example.demo.util.getsqlsession;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SqlSessionUtil {
    /**
     * 建立一个sqlSession的数据库连接
     *
     * @return 返回一个sqlSession
     */
    public SqlSession getSqlSession() {
        // 指定全局配置文件
        String resource = "mybatis-config.xml";
        // 读取配置文件
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构建sqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        // 获取sqlSession
        return sqlSessionFactory.openSession();
    }

    /**
     * 关闭数据库连接
     */
    public void closeSqlSession(SqlSession sqlSession) {
        sqlSession.close();
    }
}
