package server;

import com.wxsk.protobuf.StudentTeacher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.ReferenceCountUtil;

public class DefaultServer {

    private static int port = 5060;
    private static String host = "localhost";

    public static void main(String[] args){

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {

                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new ProtobufVarint32FrameDecoder());
                            p.addLast(new ProtobufDecoder(StudentTeacher.Student.getDefaultInstance()));

                            p.addLast(new ProtobufVarint32LengthFieldPrepender());
                            p.addLast(new ProtobufEncoder());

                            p.addLast(new EchoHandler());

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

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


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, StudentTeacher.Student msg) throws Exception {
            System.out.println("Receive a Student");
            System.out.println("name: " + msg.getName());
            ctx.writeAndFlush(msg);
        }

        public EchoHandler() {
            super(false);
        }
    }

}
