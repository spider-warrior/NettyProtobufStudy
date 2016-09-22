package client;

import com.wxsk.protobuf.StudentTeacher;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.ReferenceCountUtil;

import java.util.Scanner;

public class DefaultClient {

    private static int port = 5060;
    private static String host = "localhost";

    public static void main(String[] args) throws Exception {

        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {

                        ChannelPipeline p = ch.pipeline();

                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(StudentTeacher.Student.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        p.addLast(new EchoHandler());
                    }
                });
        ChannelFuture f = bootstrap.connect(host, port).sync();
        f.channel().closeFuture().sync();

    }

    private static class EchoHandler extends SimpleChannelInboundHandler<StudentTeacher.Student> {

//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            try {
//                ByteBuf bytebuf = (ByteBuf)msg;
//                byte[] content = new byte[bytebuf.readableBytes()];
//                bytebuf.readBytes(content);
//                System.out.println("receive a message: " + new String(content));
//            } catch (Exception e) {
//                e.printStackTrace();
//                ctx.close();
//            } finally {
//                ReferenceCountUtil.release(msg);
//            }
//        }
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            System.out.println("connection established successfully....");
//            final Channel channel = ctx.channel();
//            new Thread() {
//                @Override
//                public void run() {
//                    Scanner scanner = new Scanner(System.in);
//                    while(true) {
//                        System.out.println("请输入消息: ");
//                        byte[] msgBytes = scanner.nextLine().getBytes();
//                        ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(msgBytes.length);
//                        byteBuf.writeBytes(msgBytes);
//                        channel.writeAndFlush(byteBuf);
//                    }
//                }
//            }.start();
//        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("connection has been established successfully....");
            final Channel channel = ctx.channel();
            new Thread() {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while(true) {
                        System.out.println("请输入姓名: ");
                        String name = scanner.nextLine();
                        StudentTeacher.Student.Builder studentBuilder = StudentTeacher.Student.newBuilder();
                        studentBuilder.setName(name);
                        channel.writeAndFlush(studentBuilder.build());
                    }
                }
            }.start();

        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, StudentTeacher.Student msg) throws Exception {
            System.out.println("Receive a Student");
            System.out.println("name: " + msg.getName());
        }
    }
}
