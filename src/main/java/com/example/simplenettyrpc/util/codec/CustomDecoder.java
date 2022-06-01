package com.example.simplenettyrpc.util.codec;

import com.example.simplenettyrpc.util.serializer.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.protostuff.ProtobufIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义的解码器，结合自定义的RPC request请求协议，解决粘包和拆包问题
 */
public class CustomDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CustomDecoder.class);
    //基于ProtocolBuf的序列化器
    private static final ProtostuffSerializer serializer = new ProtostuffSerializer();
    private Class<?> toClass;

    public CustomDecoder(Class<?> toClass) {
        this.toClass = toClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //消息编解码时开始4个字节表示消息的长度，也就是消息编码的时候，先写消息的长度，再写消息
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex(); //mark一下当前读索引
        int frameLength = in.readInt(); //读取前4个字节，获取消息长度
        if (in.readableBytes() < frameLength) {
            in.resetReaderIndex(); //如果可读取的消息字节数小于frameLength，则重置读索引，这样避免了拆包
            return;
        }
        //按照frameLength长度，读取ByteBuf中的字节数据
        byte[] data = new byte[frameLength];
        in.readBytes(data); //从ByteBuf中读取frameLength长度的字节数据，到data字节数组中
        //进行反序列化，字节流 ==> object
        try {
            Object object = serializer.deserialize(data, toClass);
            out.add(object);
        } catch (Exception e) {
            logger.error("decode exception: " + e.getMessage());
        }
    }
}
