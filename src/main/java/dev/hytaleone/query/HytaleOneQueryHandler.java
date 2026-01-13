package dev.hytaleone.query;

import com.hypixel.hytale.logger.HytaleLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Netty handler that intercepts UDP packets and handles query requests.
 * Non-query packets are passed through to the QUIC codec.
 */
public class HytaleOneQueryHandler extends ChannelInboundHandlerAdapter {

    @Nonnull
    private final HytaleLogger logger;

    public HytaleOneQueryHandler(@Nonnull HytaleLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    @Override
    public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object msg) throws Exception {
        if (msg instanceof DatagramPacket packet) {
            ByteBuf content = packet.content();

            if (HytaleOneQueryProtocol.isQueryRequest(content)) {
                handleQuery(ctx, packet);
                return;
            }
        }

        // Not a query packet, pass through to QUIC codec
        ctx.fireChannelRead(msg);
    }

    private void handleQuery(@Nonnull ChannelHandlerContext ctx, @Nonnull DatagramPacket request) {
        try {
            byte queryType = HytaleOneQueryProtocol.getQueryType(request.content());

            logger.at(Level.FINE).log("Query request (type=%d) from %s",
                    queryType, request.sender());

            ByteBuf response;
            if (queryType == HytaleOneQueryProtocol.TYPE_FULL) {
                response = HytaleOneQueryProtocol.buildFullResponse(ctx.alloc());
            } else {
                response = HytaleOneQueryProtocol.buildBasicResponse(ctx.alloc());
            }

            ctx.writeAndFlush(new DatagramPacket(response, request.sender()));

        } catch (Exception e) {
            logger.at(Level.WARNING).withCause(e).log("Failed to process query from %s",
                    request.sender());
        } finally {
            request.release();
        }
    }

    @Override
    public void exceptionCaught(@Nonnull ChannelHandlerContext ctx, @Nonnull Throwable cause) {
        logger.at(Level.WARNING).withCause(cause).log("Exception in query handler");
        ctx.fireExceptionCaught(cause);
    }
}
