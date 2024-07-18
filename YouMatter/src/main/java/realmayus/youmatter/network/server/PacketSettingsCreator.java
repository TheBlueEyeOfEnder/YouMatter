package realmayus.youmatter.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacketSettingsCreator(boolean isActivated) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("youmatter", "packet_change_settings_creator");

    public PacketSettingsCreator(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(isActivated);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
