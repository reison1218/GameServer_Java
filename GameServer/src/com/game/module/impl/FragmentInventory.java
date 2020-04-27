package com.game.module.impl;

import com.base.module.AnnModule;
import com.base.module.JsonDataModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.game.player.GamePlayer;

/**
 * 
 * @author reison
 *碎片模块
 */
@AnnModule(name = "fragment", comment = "稀有碎片", tableType = TableType.JSON_TABLE_ONLY, type = ModuleType.PLAYER_MODULE)
public class FragmentInventory extends JsonDataModule{

	public FragmentInventory(GamePlayer player) {
		super(player);
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void send() {
		
	}

	@Override
	public void sendSome(Object... params) {
		
	}

	@Override
	public boolean isOpen() {
		return true;
	}

}
