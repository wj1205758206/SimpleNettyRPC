package com.example.simplenettyrpc.util.serializer;

import com.example.simplenettyrpc.domain.UserInfo;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffSerializer {
    private static final Logger logger = LoggerFactory.getLogger(ProtostuffSerializer.class);

    //复用buffer，避免每一次序列化都要分配buffer，默认buffer size为512，底层是一个byte[]
    private static final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    //序列化和反序列化都会用到一个类的Schema，使用Map结合缓存class对应的schema
    private static final Map<Class<?>, Schema<?>> schemaCached = new ConcurrentHashMap<Class<?>, Schema<?>>();

    /**
     * 序列化，将object对象序列化成字节数组
     *
     * @param object
     * @param <T>
     * @return
     */
    public <T> byte[] serialize(T object) {
        byte[] data = null;
        try {
            // 获取object对象的schema，这里schema相当于表示一个对象的组织结构，比如字段的类型，以及字段对应的值
            Schema<T> schema = (Schema<T>) getSchema(object.getClass());
            // 将object对象按照schema结构，序列化成字节流保存到buffer缓存中
            // toByteArray底层序列化的实现，实际上是调用writeTo()方法，把对象信息转换成CharSequence字符序列，再调用writeString()方法进行序列化
            data = ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            logger.error("serialize exception: " + e.getMessage());
        } finally {
            buffer.clear(); //清空buffer，复用
        }
        return data;
    }

    /**
     * 反序列化，将字节数组反序列化成指定的Class类型的对象
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            Schema<T> schema = getSchema(clazz);
            T object = schema.newMessage();
            // 将字节流数组根据schema结构反序列化成object对象
            // mergeFrom方法进行反序列化，实际上底层实现原理是遍历所有的字段，通过readString()方法从字节数组读取数据并赋值给字段，进而组装成一个object对象
            ProtostuffIOUtil.mergeFrom(data, object, schema);
            return object;
        } catch (Exception e) {
            logger.error("deserialize exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * 缓存class对应的schema
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Schema<T> getSchema(Class<T> clazz) {
        if (schemaCached.containsKey(clazz)) {
            return (Schema<T>) schemaCached.get(clazz);
        }
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        schemaCached.put(clazz, schema);
        return schema;
    }

    //test
    public static void main(String[] args) {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("张三");
        userInfo.setAge(12);
        userInfo.setId("1");
        ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();
        byte[] serialize = protostuffSerializer.serialize(userInfo);
        System.out.println(Arrays.toString(serialize));
        UserInfo deserialize = protostuffSerializer.deserialize(serialize, UserInfo.class);
        System.out.println(deserialize);
    }
}
