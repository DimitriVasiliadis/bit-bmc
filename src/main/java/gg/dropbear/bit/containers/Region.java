package gg.dropbear.bit.containers;

import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class Region {

    private final List<UUID> contributors = new ArrayList<>();
    private final OfflinePlayer owner;
    private final List<Vector3> locs;




    // region cuboid constructor
    public Region(OfflinePlayer owner, List<Vector3> locs) {
        this.owner = owner;
        this.locs = locs;


    }


    public void addContributor(OfflinePlayer p) {
        this.contributors.add(p.getUniqueId());
    }

//    @Override
//    public @NotNull Map<String, Object> serialize() {
//        final Map<String,Object> args = new HashMap<>();
//        args.put("owner", owner.getUniqueId().toString());
//        args.put("contributors", contributors);
//        args.put("locs", locs);
//
//        return args;
//    }
//
//    public static Region deserialize(Map<String, Object> args) {
//        final OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString((String) args.get("owner")));
//        final List<UUID> contributors = (List<UUID>) args.get("contributors");
//        final Location pos1 = (Location) args.get("pos1");
//        final Location pos2 = (Location) args.get("pos2");
//
//        final Region region = new Region(owner, locs);
//
//        contributors.forEach(contributor -> region.addContributor(Bukkit.getOfflinePlayer(contributor)));
//
//        return region;
//    }

}
