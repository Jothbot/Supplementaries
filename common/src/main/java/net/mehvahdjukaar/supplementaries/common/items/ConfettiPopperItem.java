package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfettiPopperItem extends Item {

    public ConfettiPopperItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, InteractionHand hand) {
        if (!level.isClientSide) {

            //no clue why im doing this from server side
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(0.2)).add(0d, -0.25, 0d);
            //hack
            float oldRot = player.getXRot();
            player.setXRot(oldRot - 20);
            ClientBoundParticlePacket packet = new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Type.CONFETTI,
                    null, player.getLookAngle());
            player.setXRot(oldRot);
            ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, BlockPos.containing(pos), packet);
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
    }
}
