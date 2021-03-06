package net.zhuoweizhang.raspberryjuice.cmd;

import net.zhuoweizhang.raspberryjuice.RemoteSession;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CmdEvent {
    private final String preFix = "events.";
    private RemoteSession session;

    public CmdEvent(RemoteSession session) {
        this.session = session;
    }

    public void execute(String command, String[] args) {
        // events.clear
        if (command.equals("clear")) {
            session.interactEventQueue.clear();
            session.chatPostedQueue.clear();
            session.projectileHitQueue.clear();

            // events.block.hits
        } else if (command.equals("block.hits")) {
            StringBuilder b = new StringBuilder();
            PlayerInteractEvent event;
            while ((event = session.interactEventQueue.poll()) != null) {
                Block block = event.getClickedBlock();
                Location loc = block.getLocation();
                b.append(session.blockLocationToRelative(loc));
                b.append(",");
                b.append(RemoteSession.blockFaceToNotch(event.getBlockFace()));
                b.append(",");
                b.append(event.getPlayer().getEntityId());
                if (session.interactEventQueue.size() > 0) {
                    b.append("|");
                }
            }
            session.send(b.toString());

            // events.chat.posts
        } else if (command.equals("chat.posts")) {
            StringBuilder b = new StringBuilder();
            AsyncPlayerChatEvent event;
            while ((event = session.chatPostedQueue.poll()) != null) {
                b.append(event.getPlayer().getEntityId());
                b.append(",");
                b.append(event.getMessage());
                if (session.chatPostedQueue.size() > 0) {
                    b.append("|");
                }
            }
            session.send(b.toString());
            // events.projectile.hits
        } else if (command.equals("projectile.hits")) {
            StringBuilder b = new StringBuilder();
            
            ProjectileHitEvent event;
            while ((event = session.projectileHitQueue.poll()) != null) {
            	Arrow arrow = (Arrow) event.getEntity();
            	LivingEntity shooter = (LivingEntity)arrow.getShooter();
            	if (shooter instanceof Player) {
					Player player = (Player)shooter;
					Block block = arrow.getAttachedBlock(); 
					if (block == null)
						block = arrow.getLocation().getBlock();
					Location loc = block.getLocation();
					b.append(session.blockLocationToRelative(loc));
					b.append(",");
					b.append(1);
					b.append(",");
					b.append(player.getPlayerListName());
					b.append(",");
					Entity hitEntity = event.getHitEntity();
					if(hitEntity!=null){
						if(hitEntity instanceof Player){	
							Player hitPlayer = (Player)hitEntity;
							b.append(hitPlayer.getPlayerListName());
						}else{
							b.append(hitEntity.getName());
						}
					}
				}
				
                if (session.interactEventQueue.size() > 0) {
                    b.append("|");
                    arrow.remove();
                }
            }
            session.send(b.toString());

        } else {
            session.plugin.getLogger().warning(preFix + command + " is not supported.");
            session.send("Fail," + preFix + command + " is not supported.");
        }
    }
}
