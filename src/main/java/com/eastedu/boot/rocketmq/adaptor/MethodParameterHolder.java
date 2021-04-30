package com.eastedu.boot.rocketmq.adaptor;

import com.aliyun.openservices.ons.api.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法参数
 *
 * @author luozhenzhong
 */
public class MethodParameterHolder {
    private final Type type;
    private final List<Type> actualType = new ArrayList<>();
    private final int parameterIndex;

    public MethodParameterHolder(Type type, int parameterIndex) {
        this.type = type;
        this.parameterIndex = parameterIndex;
    }

    public Type getType() {
        return type;
    }

    public List<Type> getActualType() {
        return actualType;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void addTypedParameter(Type clazz) {
        this.actualType.add(clazz);
    }

    public boolean check(boolean batch) {
        if (batch) {
            return type.getTypeName().equals(List.class.getName())
                    && !actualType.isEmpty()
                    && actualType.get(0).getTypeName().equals(Message.class.getName());
        } else {
            return type.getTypeName().equals(Message.class.getName());
        }
    }
}
