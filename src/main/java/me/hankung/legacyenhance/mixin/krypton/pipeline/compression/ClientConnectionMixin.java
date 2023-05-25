package me.hankung.legacyenhance.mixin.krypton.pipeline.compression;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.Natives;

import io.netty.channel.Channel;
import me.hankung.legacyenhance.utils.krypton.compress.MinecraftCompressDecoder;
import me.hankung.legacyenhance.utils.krypton.compress.MinecraftCompressEncoder;
import net.minecraft.network.ClientConnection;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Shadow
    private Channel channel;

    @Inject(method = "setCompressionThreshold", at = @At("HEAD"), cancellable = true)
    public void legacy$setCompressionThreshold(int compressionThreshold, CallbackInfo ci) {
        if (compressionThreshold >= 0) {
            VelocityCompressor compressor = Natives.compress.get().create(4);
            MinecraftCompressEncoder encoder = new MinecraftCompressEncoder(compressionThreshold, compressor);
            MinecraftCompressDecoder decoder = new MinecraftCompressDecoder(compressionThreshold, compressor);

            channel.pipeline().addBefore("decoder", "decompress", decoder);
            channel.pipeline().addBefore("encoder", "compress", encoder);
        } else {
            this.channel.pipeline().remove("decompress");
            this.channel.pipeline().remove("compress");
        }

        ci.cancel();
    }

}
