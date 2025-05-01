package com.rpgfarm.BawServiceHTTP.commands;

import com.rpgfarm.BawServiceHTTP.Main;
import com.rpgfarm.BawServiceHTTP.apis.SocketAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class MonetizeCommand implements Listener {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) {
            sender.sendMessage("관리자만 사용할 수 있습니다.");
            return false;
        }
        if (command.getName().equalsIgnoreCase("minehubmonetize")) {
            if (args.length == 0) {
                sender.sendMessage("/minehubmonetize reload: 설정을 다시 불러옵니다.");
            } else if (args[0].equalsIgnoreCase("reload")) {
                SocketAPI.stopSocketThread();
                SocketAPI.startSocketThread();
                sender.sendMessage("설정을 다시 불러왔습니다.");
                Main.singleInstance.getLogger().info("설정을 다시 불러왔습니다.");
            }
        }
        return false;
    }
}
