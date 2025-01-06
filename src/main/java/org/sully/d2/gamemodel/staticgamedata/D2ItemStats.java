package org.sully.d2.gamemodel.staticgamedata;

import java.util.HashSet;
import java.util.Set;

public enum D2ItemStats {

	STRENGTH(0),
	ENERGY(1),
	DEXTERITY(2),
	VITALITY(3),
	
	LIFE(7),
	MANA(8),
	STAMINA(11),
	
	GOLD_AMOUNT(14),
	ENHANCED_DEFENSE_PERCENT(16),
	MAXDAMAGE_PERCENT(17),
	ATTACK_RATING(19),
	
	MIN_DAMAGE(21),
	MAX_DAMAGE(22),
	
	SECONDARY_MIN_DAMAGE(23),
	SECONDARY_MAX_DAMAGE(24),
	
	FIRE_RESIST(39),
	LIGHTNING_RESIST(41),
	COLD_RESIST(43),
	POISON_RESIST(45),
	MAGIC_RESIST(37),
	DAMAGE_RESIST(36),
	MAGIC_DAMAGE_REDUCTION(35),
	DAMAGE_REDUCTION(34),
	
	
	GOLD_FIND(79),
	MAGIC_FIND(80),
	
	ATTACK_RATING_PERCENT(119),
	DAMAGE_TO_DEMONS_PERCENT(121),
	DAMAGE_TO_UNDEAD_PERCENT(122),
	ATTACK_RATING_TO_DEMONS(123),
	ATTACK_RATING_TO_UNDEAD(124),
	
	CRUSHING_BLOW_PERCENT(136),
	DEADLY_STRIKE_PERCENT(141),
	
	MANA_AFTER_EACH_KILL(138),
	
	REDUCE_REQUIREMENTS_PERCENT(91),

	INCREASED_ATTACK_SPEED(93),
	FASTER_BLOCK_RATE(102),
	FASTER_RUN_WALK_SPEED(96),
	FASTER_HIT_RECOVERY(99),
	FASTER_CAST_RATE(105),
	
	SOCKETS(194),
	
	LIFE_PER_LEVEL(216),
	MANA_PER_LEVEL(217),
	ATTACK_RATING_PER_LEVEL(224),
	ATTACK_RATING_PERCENT_PER_LEVEL(225),
	MAX_DAMAGE_PER_LEVEL(218)
	
	;
	
	public final int statId;

	public D2ItemStat get() {
		return D2ItemStat.fromId(statId);
	}
	
	D2ItemStats(int statId) {
		this.statId = statId;
	}
	
	public static Set<Integer> getStatIds(Iterable<D2ItemStats> stats) {
		Set<Integer> statIds = new HashSet<>();
		for (D2ItemStats stat: stats) {
			statIds.add(stat.statId);
		}
		return statIds;
	}
	
	public static Set<Integer> getStatIds(D2ItemStats...stats) {
		Set<Integer> statIds = new HashSet<>();
		for (D2ItemStats stat: stats) {
			statIds.add(stat.statId);
		}
		return statIds;
	}

}
