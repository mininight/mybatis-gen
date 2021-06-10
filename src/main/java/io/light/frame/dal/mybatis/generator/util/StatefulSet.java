/*
 *  Copyright © 2018 - 2021 xulianqiang90@163.com. All Rights Reserved.
 */

package io.light.frame.dal.mybatis.generator.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Stateful set
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-05-16 01:20
 */
public class StatefulSet<E> extends LinkedHashSet<E> {

    private volatile boolean completed = false;

    public StatefulSet() {
        super();
    }

    public StatefulSet(int initialCapacity) {
        super(initialCapacity);
    }

    public StatefulSet(Collection<? extends E> c) {
        super(c);
    }

    public static void markCompleted(Object target) {
        if (target == null || target instanceof Class) {
            return;
        }
        Class<?> clazz = target.getClass();
        if (clazz.isPrimitive() || clazz.isAnnotation() || clazz.isInterface()) {
            return;
        }
        if (target instanceof Map) {
            for (Object item : ((Map<?, ?>) target).values()) {
                markCompleted(item);
            }
            return;
        }
        if (target instanceof Collection) {
            for (Object item : (Collection<?>) target) {
                markCompleted(item);
            }
            return;
        }
        if (target.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(target); i++) {
                markCompleted(Array.get(target, i));
            }
            return;
        }
        ReflectionUtils.doWithFields(clazz, field -> {
            Object value;
            try {
                value = field.get(target);
            } catch (Exception e) {
                value = null;
            }
            if (value == null) {
                return;
            }
            if (!(value instanceof StatefulSet)) {
                markCompleted(value);
                return;
            }
            StatefulSet<?> statefulSet = (StatefulSet<?>) value;
            statefulSet.complete();
        });
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        if (isCompleted()) {
            return;
        }
        synchronized (this) {
            this.completed = true;
        }
    }

    @Override
    public boolean add(E e) {
        if (isCompleted()) {
            throw new UnsupportedOperationException();
        }
        synchronized (this) {
            if (isCompleted()) {
                throw new UnsupportedOperationException();
            }
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (isCompleted()) {
            throw new UnsupportedOperationException();
        }
        synchronized (this) {
            if (isCompleted()) {
                throw new UnsupportedOperationException();
            }
            return super.addAll(c);
        }
    }
}
