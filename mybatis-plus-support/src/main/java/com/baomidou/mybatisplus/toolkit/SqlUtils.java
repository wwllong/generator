/**
 * Copyright (c) 2011-2020, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.mybatisplus.toolkit;

import java.util.List;

import com.baomidou.mybatisplus.enums.SqlLike;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.baomidou.mybatisplus.plugins.parser.ISqlParser;
import com.baomidou.mybatisplus.plugins.parser.SqlInfo;

/**
 * <p>
 * SqlUtils工具类
 * </p>
 *
 * @author Caratacus
 * @Date 2016-11-13
 */
public class SqlUtils {

    private final static SqlFormatter sqlFormatter = new SqlFormatter();
    public final static String SQL_BASE_COUNT = "SELECT COUNT(1) FROM ( %s ) TOTAL";
    public static ISqlParser COUNT_SQL_PARSER = null;
    private static Class<ISqlParser> DEFAULT_CLASS = null;

    static {
        try {
            DEFAULT_CLASS = (Class<ISqlParser>) Class.forName("com.baomidou.mybatisplus.plugins.pagination.optimize.JsqlParserCountOptimize");
        } catch (ClassNotFoundException e) {
            //skip
        }
    }

    /**
     * <p>
     * 获取CountOptimize
     * </p>
     *
     * @param sqlParser   Count SQL 解析类
     * @param originalSql 需要计算Count SQL
     * @return SqlInfo
     */
    public static SqlInfo getCountOptimize(ISqlParser sqlParser, String originalSql) {
        // COUNT SQL 解析器
        if (null == COUNT_SQL_PARSER) {
            if (null != sqlParser) {
                // 用户自定义 COUNT SQL 解析
                COUNT_SQL_PARSER = sqlParser;
            } else {
                // 默认 JsqlParser 优化 COUNT
                try {
                    // TODO: 2017/11/20 这里我改动了下.
                    COUNT_SQL_PARSER = DEFAULT_CLASS.newInstance();
                } catch (Exception e) {
                    throw new MybatisPlusException(e);
                }
            }
        }
        return COUNT_SQL_PARSER.optimizeSql(null, originalSql);
    }

    /**
     * 查询SQL拼接Order By
     *
     * @param originalSql 需要拼接的SQL
     * @param page        page对象
     * @param orderBy     是否需要拼接Order By
     * @return
     */
    public static String concatOrderBy(String originalSql, Pagination page, boolean orderBy) {
        if (orderBy && page.isOpenSort()) {
            StringBuilder buildSql = new StringBuilder(originalSql);
            String ascStr = concatOrderBuilder(page.getAsc(), " ASC");
            String descStr = concatOrderBuilder(page.getDesc(), " DESC");
            if (StringUtils.isNotEmpty(ascStr) && StringUtils.isNotEmpty(descStr)) {
                ascStr += ", ";
            }
            if (StringUtils.isNotEmpty(ascStr) || StringUtils.isNotEmpty(descStr)) {
                buildSql.append(" ORDER BY ").append(ascStr).append(descStr);
            }
            return buildSql.toString();
        }
        return originalSql;
    }

    /**
     * 拼接多个排序方法
     *
     * @param columns
     * @param orderWord
     */
    private static String concatOrderBuilder(List<String> columns, String orderWord) {
        if (CollectionUtils.isNotEmpty(columns)) {
            StringBuilder builder = new StringBuilder(16);
            for (int i = 0; i < columns.size(); ) {
                String cs = columns.get(i);
                if (StringUtils.isNotEmpty(cs)) {
                    builder.append(cs).append(orderWord);
                }
                if (++i != columns.size() && StringUtils.isNotEmpty(cs)) {
                    builder.append(", ");
                }
            }
            return builder.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 格式sql
     *
     * @param boundSql
     * @param format
     * @return
     */
    public static String sqlFormat(String boundSql, boolean format) {
        if (format) {
            return sqlFormatter.format(boundSql);
        } else {
            return boundSql.replaceAll("[\\s]+", " ");
        }
    }

    /**
     * <p>
     * 用%连接like
     * </p>
     *
     * @param str 原字符串
     * @return
     */
    public static String concatLike(String str, SqlLike type) {
        StringBuilder builder = new StringBuilder(str.length() + 3);
        switch (type) {
            case LEFT:
                builder.append("%").append(str);
                break;
            case RIGHT:
                builder.append(str).append("%");
                break;
            case CUSTOM:
                builder.append(str);
                break;
            default:
                builder.append("%").append(str).append("%");
        }
        return builder.toString();
    }

}
