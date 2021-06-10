package io.light.frame.dal.mybatis.generator.sql.meta.opt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.light.frame.dal.mybatis.generator.sql.Dialect;
import io.light.frame.dal.mybatis.generator.sql.meta.MetaAccessor;
import io.light.frame.dal.mybatis.generator.util.DialectJdbcTemplate;
import io.light.frame.dal.mybatis.generator.util.GenToolKit;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Type;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base meta operations
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 02:24
 */
public abstract class BaseMetaOperations implements MetaOperations {

    private volatile static MetaAccessor metaAccessor;
    private final Dialect dialect;

    protected BaseMetaOperations(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public final Dialect getDialect() {
        return dialect;
    }

    protected final DialectJdbcTemplate lookupJdbcTemplate() {
        if (metaAccessor == null) {
            metaAccessor = GenToolKit.beanFactory().getBean(MetaAccessor.class);
        }
        return metaAccessor.lookupJdbcTemplate();
    }

    protected final <T> RowMapper<T> newRowMapper(Class<T> type) {
        return newRowMapper((Type) type, null);
    }

    protected final <T> RowMapper<T> newRowMapper(Class<T> type, BiConsumer<Type, JSONObject> mappingEditor) {
        return newRowMapper((Type) type, mappingEditor);
    }

    protected final <T> RowMapper<T> newRowMapper(TypeReference<T> typeRef) {
        return newRowMapper(typeRef.getType(), null);
    }

    protected final <T> RowMapper<T> newRowMapper(TypeReference<T> typeRef, BiConsumer<Type, JSONObject> mappingEditor) {
        return newRowMapper(typeRef.getType(), mappingEditor);
    }

    protected final <T> RowMapper<T> newRowMapper(Type type) {
        return newRowMapper(type, null);
    }

    @SuppressWarnings("unchecked")
    protected <T> RowMapper<T> newRowMapper(Type type, BiConsumer<Type, JSONObject> mappingEditor) {
        return (rs, rowNum) -> {
            ResultSetMetaData metaData = rs.getMetaData();
            int colNum = metaData.getColumnCount();
            if (colNum <= 1) {
                Object colValue = rs.getObject(0);
                if (colValue == null) {
                    return null;
                } else {
                    if (type instanceof Class && ((Class<?>) type).isAssignableFrom(colValue.getClass())) {
                        return (T) colValue;
                    }
                    return JSON.parseObject(String.valueOf(colValue), type);
                }
            } else {
                JSONObject mapping = new JSONObject();
                for (int i = 1; i <= colNum; i++) {
                    String colName = JdbcUtils.lookupColumnName(metaData, i);
                    Object colValue = rs.getObject(i);
                    mapping.put(colName.toLowerCase(), colValue);
                }
                if (mapping.isEmpty()) {
                    return null;
                }
                if (mappingEditor != null) {
                    mappingEditor.accept(type, mapping);
                }
                return mapping.toJavaObject(type);
            }
        };
    }

    protected void editMapping(JSONObject mapping, Function<Map.Entry<String, Object>, String> editor) {
        Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
        Map<String, Object> newPairs = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            if (editor != null) {
                String finalKey = editor.apply(entry);
                if (!key.equals(finalKey)) {
                    iterator.remove();
                    newPairs.put(finalKey, entry.getValue());
                }
            }
        }
        if (!newPairs.isEmpty()) {
            mapping.putAll(newPairs);
        }
    }

}
