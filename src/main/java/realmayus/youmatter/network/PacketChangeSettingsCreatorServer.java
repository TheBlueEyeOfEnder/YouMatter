package realmayus.youmatter.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import realmayus.youmatter.creator.CreatorContainer;
//import realmayus.youmatter.creator.ContainerCreator;
import java.util.function.Supplier;

public class PacketChangeSettingsCreatorServer{

    private boolean isActivated;


    public PacketChangeSettingsCreatorServer(FriendlyByteBuf buf) {
        isActivated = buf.readBoolean();
    }

    public PacketChangeSettingsCreatorServer(boolean isActivated) {
        this.isActivated = isActivated;
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isActivated);
    }

    void handle(Supplier<NetworkEvent.Context> ctx) {
        // This is the player the packet was sent to the server from
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player.containerMenu instanceof CreatorContainer) {
                CreatorContainer openContainer = (CreatorContainer) player.containerMenu;
                openContainer.te.setActivated(isActivated);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
