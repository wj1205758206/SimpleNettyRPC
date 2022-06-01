package com.example.simplenettyrpc.util.codec;

import com.example.simplenettyrpc.util.serializer.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义的编码器，结合自定义的RPC response响应协议，解决粘包和拆包问题
 */
public class CustomEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(CustomEncoder.class);
    //基于ProtocolBuf的序列化器
    private static final ProtostuffSerializer serializer = new ProtostuffSerializer();
    private Class<?> toClass;

    public CustomEncoder(Class<?> toClass) {
        this.toClass = toClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (toClass.isInstance(msg)) {
            try {
                //将msg序列化成字节流
                byte[] data = serializer.serialize(msg);
                //先向ByteBuf中写入一个int类型的值，即用4个字节来表示msg信息的长度
                out.writeInt(data.length);
                //再写入具体的msg数据
                out.writeBytes(data);
            } catch (Exception e) {
                logger.error("encode exception: " + e.getMessage());
            }
        }
    }
}
