package realmayus.youmatter.network;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import realmayus.youmatter.creator.CreatorMenu;
import realmayus.youmatter.network.server.PacketSettingsCreator;
import realmayus.youmatter.network.server.PacketSettingsReplicator;
import realmayus.youmatter.network.server.PacketShowNext;
import realmayus.youmatter.network.server.PacketShowPrevious;
import realmayus.youmatter.replicator.ReplicatorMenu;

public class PacketHandler {
    private PacketHandler() {
        // Private constructor to prevent instantiation
    }

    public static class CreatorSettings {
        private static CreatorSettings instance;

        private CreatorSettings() {}

        public static synchronized CreatorSettings getInstance() {
            if (instance == null) {
                instance = new CreatorSettings();
            }
            return instance;
        }

        public void handle(final PacketSettingsCreator data, final PlayPayloadContext ctx) {
            // This is the player the packet was sent to the server from
            ctx.workHandler().submitAsync(() -> {
                if (ctx.player().get().containerMenu instanceof CreatorMenu openContainer) {
                    openContainer.creator.setActivated(data.isActivated());
                }
            });
        }
    }

    public static class ShowNext {
        private static ShowNext instance;

        private ShowNext() {}

        public static synchronized ShowNext getInstance() {
            if (instance == null) {
                instance = new ShowNext();
            }
            return instance;
        }

        public void handle(final PacketShowNext data, final PlayPayloadContext ctx) {
            ctx.workHandler().submitAsync(() -> {
                if (ctx.player().get().containerMenu instanceof ReplicatorMenu openContainer) {
                    openContainer.replicator.renderNext();
                }
            });
        }
    }

    public static class ShowPrevious {
        private static ShowPrevious instance;

        private ShowPrevious() {}

        public static synchronized ShowPrevious getInstance() {
            if (instance == null) {
                instance = new ShowPrevious();
            }
            return instance;
        }

        public void handle(final PacketShowPrevious data, final PlayPayloadContext ctx) {
            ctx.workHandler().submitAsync(() -> {
                if (ctx.player().get().containerMenu instanceof ReplicatorMenu openContainer) {
                    openContainer.replicator.renderPrevious();
                }
            });
        }
    }

    public static class ReplicatorSettings {
        private static ReplicatorSettings instance;

        private ReplicatorSettings() {}

        public static synchronized ReplicatorSettings getInstance() {
            if (instance == null) {
                instance = new ReplicatorSettings();
            }
            return instance;
        }

        public void handle(final PacketSettingsReplicator data, final PlayPayloadContext ctx) {
            ctx.workHandler().submitAsync(() -> {
                if (ctx.player().get().containerMenu instanceof ReplicatorMenu openContainer) {
                    openContainer.replicator.setActive(data.isActivated());
                    openContainer.replicator.setCurrentMode(data.mode());
                }
            });
        }
    }
}

