package org.sully.d2.gamemodel.staticgamedata;

import java.util.HashSet;
import java.util.Set;

public enum D2ItemStats {

	STRENGTH(0),
	ENERGY(1),
	DEXTERITY(2),
	VITALITY(3),
	
	LIFE(7), // +(stat.value/256) to Life
	MANA(8), // +(stat.value/256) to Mana
	STAMINA(11), // +(stat.value/256) to Stamina
	
	GOLD_AMOUNT(14),
	ENHANCED_DEFENSE_PERCENT(16), // + (stat.value)% Enhanced Defense
	MAXDAMAGE_PERCENT(17), // value 15 means +15% Enhanced Damage. Items will have both stats 17 and 18
	ATTACK_RATING(19), // value 10 means +10 Attack Rating

	INCREASED_BLOCK_CHANCE(20), // {stat.value} % increased chance of blocking

	MIN_DAMAGE(21), // example Ring of Performance (+10 to minimum damage)
	MAX_DAMAGE(22),
	
	SECONDARY_MIN_DAMAGE(23), // Ring of Performance has both stats 21 and 23 and 159 (duplicate)
	SECONDARY_MAX_DAMAGE(24), // example Pike of Maiming (+3 to Max Damage)

	HEAL_STAMINA(28), // Heal Stamina Plus {stat.value} %
	DEFENSE(31), // +{stat.value} Defense (ex. on a jewel)

	DAMAGE_REDUCTION(34), // damage reduced by {stat.value}
	MAGIC_DAMAGE_REDUCTION(35), // magic damage reduced by {stat.value}
	DAMAGE_RESIST(36),
	MAGIC_RESIST(37),


	// +All Resists items will have all 4 resist stats separately
	FIRE_RESIST(39), // value = 10 means +10% Fire Resist
	LIGHTNING_RESIST(41),
	COLD_RESIST(43),
	POISON_RESIST(45),

	MIN_FIRE_DAMAGE(48),
	MAX_FIRE_DAMAGE(49),
	MIN_LIGHTNING_DAMAGE(50),
	MAX_LIGHTNING_DAMAGE(51),

	MIN_COLD_DAMAGE(54),
	MAX_COLD_DAMAGE(55),
	COLD_DURATION(56), // stat.value measured in frames (1/25 sec)

	MIN_POISON_DAMAGE(57), // stat.value = poison damage (in 1/256ths) per frame (1/25 sec)
	MAX_POISON_DAMAGE(58),
	POISON_DURATION(59), // stat.value measured in frames (1/25 sec)


	LIFE_STEAL(60), // {stat.value}% life stolen per hit
	MANA_STEAL(62), // {stat.value}% mana stolen per hit

	REPLENISH_LIFE(74), // "replenish life +{stat.value}"

	ATTACKER_TAKES_DAMAGE(78), // attacker takes damage of {stat.value}
	
	GOLD_FIND(79), // {stat.value} % extra gold from monsters
	MAGIC_FIND(80), // {stat.value} % better chance of getting magic items

	KNOCKBACK(81), // {stat.value} = 1?
	PLUS_ALL_SKILLS_IN_SINGLE_CHARACTER_CLASS(83), // +{stat.value} to {stat.param} skills (param is class.id)

	LIGHT_RADIUS(89), // + {stat.value} to light radius
	REDUCE_REQUIREMENTS_PERCENT(91), // {stat.value} IS NEGATIVE, -20 means Requirements -20%
	INCREASED_ATTACK_SPEED(93), // value 20 means +20% IAS

	FASTER_RUN_WALK_SPEED(96),// +{stat.value} % faster run walk
	FASTER_HIT_RECOVERY(99), // +{stat.value} % faster hit recovery
	FASTER_BLOCK_RATE(102), // +{stat.value} % faster block rate
	FASTER_CAST_RATE(105), // +{stat.value} % faster cast rate

	PLUS_TO_SKILL(107), // param = skillId , value = how many skill points

	POISON_LENGTH_REDUCED(110), // poison length reduced by {stat.value} %

	HIT_CAUSES_MONSTER_TO_FLEE(112), // stat.value=64 means 50% chance to flee
	DAMAGE_TAKEN_GOES_TO_MANA(114), // {stat.value} % damage taken goes to mana
	IGNORE_TARGET_DEFENSE(115), // stat.value=1

	PREVENT_MONSTER_HEAL(117), // stat.value=1
	HALF_FREEZE_DURATION(118), // stat.value=1 means "Half Freeze Duration"

	ATTACK_RATING_PERCENT(119), // +{stat.value}% bonus to attack rating
	DAMAGE_TO_DEMONS_PERCENT(121), // +{stat.value}% damage to demons
	DAMAGE_TO_UNDEAD_PERCENT(122), // +{stat.value}% damage to undead
	ATTACK_RATING_TO_DEMONS(123), // +{stat.value} to attack rating against demons
	ATTACK_RATING_TO_UNDEAD(124), // +{stat.value} to attack rating against undead
	
	CRUSHING_BLOW_PERCENT(136),
	MANA_AFTER_EACH_KILL(138), // +{stat.value} to mana after each kill
	DEADLY_STRIKE_PERCENT(141),

	INDESTRUCTIBLE(152), // stat.value = 1
	SLOWER_STAMINA_DRAIN(154), // {stat.value}% slower stamina drain

	MIN_DAMAGE_FOR_THROWING_ITEM(159),
	MAX_DAMAGE_FOR_THROWING_ITEM(160),

	PLUS_TO_SKILL_TAB(188), // param = skillTabId , value = how many skill points

	SOCKETS(194),

	SKILL_PROC_ON_ATTACK(195), // param = (combination of skillId and level?) , stat.value = % chance to cast
	SKILL_PROC_ON_STRIKING(198),
	SKILL_PROC_ON_STRUCK(201),
	SKILL_CHARGES(204), // param = (combination of skillId and level?) , stat.value = (combination of number of charges remaining and total charges?)


	LIFE_PER_LEVEL(216), // {stat.value}/256/8 life per level
	MANA_PER_LEVEL(217), // {stat.value}/256/8 mana per level (stat.value=1536 means 0.75 mana per level)
	DEFENSE_PER_LEVEL(214) , // {stat.value}/8 defense per level
	MAX_DAMAGE_PER_LEVEL(218), // {stat.value}/8 max damage per level (ie. stat.value=6 means 0.75 max damage per level)
	ATTACK_RATING_PER_LEVEL(224), // {stat.value}/2 attack rating per level, ie.  ( floor( stat.value*char.level / 2 ) )
	ATTACK_RATING_PERCENT_PER_LEVEL(225), // {stat.value}/2 % bonus to attack rating per level (stat.value=2 means 1% attack rating bonus per level)

	SELF_REPAIR_DURABILITY(252), // stat.value=3 means 1 durability per 33 seconds
	REPLENISHES_QUANTITY(253), // stat.value = 10 on "Simbilan of Propogation"[
	INCREASE_STACK_SIZE(254), // increase stack size by stat.value? (ie. stat.value=63 on a rare War Javelin)

	POISON_COUNT(326) // stat.value=1 ? unknown
	
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
