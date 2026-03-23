package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.D2ItemStats;

import java.util.*;

public class AssortedMagicItemsConsumer implements D2TCDropConsumer {

    @Getter
    long totalIterations = 0L;

    @Getter
    String id;

    private long[][] counts = new long[Category.values().length][2];
    private D2Item[][] examples = new D2Item[Category.values().length][2];

    public AssortedMagicItemsConsumer(String id) {
        this.id = id;
    }

    @Override
    public void incrementFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, SerializableD2Item> itemsById) {
        AssortedMagicItemsSnapshot snapshot = (AssortedMagicItemsSnapshot) untypedSnapshot;

        totalIterations += snapshot.getTotalIterations();

        int categoryCount = snapshot.getCategories().size();
        for (int i = 0; i < categoryCount; i++) {
            if (Category.valueOf(snapshot.getCategories().get(i)).index != i) {
                throw new RuntimeException("Unexpected category index " + i + " category " + snapshot.getCategories().get(i));
            }
            counts[i][0] += snapshot.getCounts()[i][0];
            counts[i][1] += snapshot.getCounts()[i][1];
            if (snapshot.getExampleItems()[i][0] != null) examples[i][0] = D2Item.fromSerializableD2Item(itemsById.get(snapshot.getExampleItems()[i][0]));
            if (snapshot.getExampleItems()[i][1] != null) examples[i][1] = D2Item.fromSerializableD2Item(itemsById.get(snapshot.getExampleItems()[i][1]));
        }

    }

    @Override
    public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {
        Set<Long> itemIds = new HashSet<>();
        List<D2Item> itemsToSave = new ArrayList<>();
        Long[][] exampleItemIds = new Long[examples.length][2];
        for (int i = 0; i < examples.length; i++) {
            for (int j = 0; j < 2; j++) {
                if (examples[i][j] != null) {
                    if (!itemIds.contains(examples[i][j].getId())) {
                        itemIds.add(examples[i][j].getId());
                        itemsToSave.add(examples[i][j]);
                    }
                    exampleItemIds[i][j] = examples[i][j].getId();
                }
            }
        }

        return DataReferencingItems.<TCDropConsumerSnapshot>builder()
                .items(itemsToSave)
                .data(AssortedMagicItemsSnapshot.builder()
                    .id(id)
                    .totalIterations(totalIterations)
                    .counts(counts)
                    .exampleItems(exampleItemIds)
                    .categories(Arrays.stream(Category.values()).map(x -> x.name()).toList())
                    .build())
                .build();
    }

    @Override
    public void consume(D2TCDrop tcDrop) {
        totalIterations++;
        for (D2Item item : tcDrop.getItems()) {
            consumeItem(item);
        }
    }

    private void consumeItem(D2Item item) {
        if (item.getQuality() != ItemQuality.MAGIC) return;

        switch(item.getItemTypeType().getCode()) {
            case "tors" : handleTorsoArmor(item); break;
            case "shie" : handleNonPaladinShield(item); break;
            case "ashd" : handlePaladinShield(item); break;
            case "lcha" : handleGrandCharm(item); break;
            case "mcha" : handleLargeCharm(item); break;
            case "scha" : handleSmallCharm(item); break;
            case "amul" : handleAmulet(item); break;
            case "ring" : handleRing(item); break;
            case "jewl" : handleJewel(item); break;
            case "circ" : handleCirclet(item); break;
            case "glov" : handleGloves(item); break;
            case "ajav" : handleAmazonJavelin(item); break;
        }

    }




    private void addItem(Category category, D2Item item) {
        if (counts[category.index][0] == 0) {
            examples[category.index][0] = item;
            System.out.println("First " + category.name() + " : " + item.toLongString());
        }
        counts[category.index][0]++;
    }
    private void addPerfectItem(Category category, D2Item item) {
        if (counts[category.index][1] == 0) {
            examples[category.index][1] = item;
            System.out.println("First PERFECT " + category.name() + " : " + item.toLongString());
        }
        counts[category.index][1]++;
    }

    private void handleTorsoArmor(D2Item item) {
        if (item.getSockets() == 4) {
            // 4 socket Archon Plate of the whale
            if ("utp".equals(item.getItemType().getCode()) && item.getPlusLifeStat() >= 81) {
                addItem(Category.FOUR_SOCKET_ARCHON_PLATE_OF_THE_WHALE, item);
                if (item.getPlusLifeStat() == 100) {
                    addPerfectItem(Category.FOUR_SOCKET_ARCHON_PLATE_OF_THE_WHALE, item);
                }
            }
            // 4 socket any-armor of the whale
            if (item.getPlusLifeStat() >= 81) {
                addItem(Category.FOUR_SOCKET_ANY_ARMOR_OF_THE_WHALE, item);
                if (item.getPlusLifeStat() == 100) {
                    addPerfectItem(Category.FOUR_SOCKET_ANY_ARMOR_OF_THE_WHALE, item);
                }
            }

            // 4 socket any-armor of stability (24 fhr)
            if (item.getStat(D2ItemStats.FASTER_HIT_RECOVERY.statId) == 24) {
                addPerfectItem(Category.FOUR_SOCKET_ANY_ARMOR_OF_STABILIITY, item);
            }
        }

    }

    private void handleNonPaladinShield(D2Item item) {
        // Jeweler's Monarch of Deflecting
        // Jeweler's Monarch of Simplicity
        if (item.getSockets() == 4) {
            if("uit".equals(item.getItemTypeCode())) { // Monarch
                if (item.getStat(D2ItemStats.FASTER_BLOCK_RATE.statId) == 30) {
                    addPerfectItem(Category.JEWELERS_MONARCH_OF_DEFLECTING, item);
                }
                if (item.getStat(D2ItemStats.REDUCE_REQUIREMENTS_PERCENT.statId) == -30) {
                    addPerfectItem(Category.JEWELERS_MONARCH_OF_SIMPLICITY, item);
                }
            }
        }
    }


    private void handlePaladinShield(D2Item item) {
        // Jeweler's (Sacred Targe / Sacred Rondache / Kurast Shield) of Deflecting
        //    with 35-45 all resist or (101-121 attack rating + 51-65% damage)
        if (item.getSockets() == 4) {
            if("pab".equals(item.getItemTypeCode())) { // Sacred Targe
                if (item.getStat(D2ItemStats.FASTER_BLOCK_RATE.statId) == 30) { // Deflecting
                    if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 35) { // all resists will be the same
                        addItem(Category.FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_RESIST, item);
                        if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 45) {
                            addPerfectItem(Category.FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_RESIST, item);
                        }
                    }
                    if (item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) >= 51) {
                        addItem(Category.FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_DAMAGE, item);
                        if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 65) {
                            addPerfectItem(Category.FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_DAMAGE, item);
                        }
                    }
                }
            }
            // repeat but dont restrict to just Sacred Targe
            if (item.getStat(D2ItemStats.FASTER_BLOCK_RATE.statId) == 30) { // Deflecting
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 35) { // all resists will be the same
                    addItem(Category.FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_RESIST, item);
                    if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 45) {
                        addPerfectItem(Category.FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_RESIST, item);
                    }
                }
                if (item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) >= 51) {
                    addItem(Category.FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_DAMAGE, item);
                    if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 65) {
                        addPerfectItem(Category.FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_DAMAGE, item);
                    }
                }
            }

        }
    }



    private void handleGrandCharm(D2Item item) {
        if (item.getPlusLifeStat() >= 36) {
            if (item.getPlusLifeStat() <= 40) {
                if (item.hasAtLeastOneSkillTabBonus()) {
                    if (item.getSkillTabBonusLevel(SkillTab.PALADIN_COMBAT_SKILLS) > 0) {
                        addItem(Category.PCOMBAT_GRAND_CHARM_OF_VITA_36_40, item);
                        if (item.getPlusLifeStat() == 40)
                            addPerfectItem(Category.PCOMBAT_GRAND_CHARM_OF_VITA_36_40, item);
                    }
                    addItem(Category.ANY_SKILLTAB_GRAND_CHARM_OF_VITA_36_40, item);
                    if (item.getPlusLifeStat() == 40)
                        addPerfectItem(Category.ANY_SKILLTAB_GRAND_CHARM_OF_VITA_36_40, item);
                }
                if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) >= 7) { // Sharp prefix
                    addItem(Category.SHARP_GRAND_CHARM_OF_VITA_36_40, item);
                    if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 10 &&
                            item.getStat(D2ItemStats.ATTACK_RATING.statId) == 76 &&
                            item.getPlusLifeStat() == 40) {
                        addPerfectItem(Category.SHARP_GRAND_CHARM_OF_VITA_36_40, item);
                    }
                }
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 13 && item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) >= 13) {
                    addItem(Category.SHIMMERING_GRAND_CHARM_OF_VITA_36_40, item);
                    if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 15 && item.getPlusLifeStat() == 40) {
                        addPerfectItem(Category.SHIMMERING_GRAND_CHARM_OF_VITA_36_40, item);
                    }
                }
            } else { // repeat for Vita 41-45 life
                if (item.hasAtLeastOneSkillTabBonus()) {
                    if (item.getSkillTabBonusLevel(SkillTab.PALADIN_COMBAT_SKILLS) > 0) {
                        addItem(Category.PCOMBAT_GRAND_CHARM_OF_VITA_41_45, item);
                        if (item.getPlusLifeStat() == 45)
                            addPerfectItem(Category.PCOMBAT_GRAND_CHARM_OF_VITA_41_45, item);
                    }
                    addItem(Category.ANY_SKILLTAB_GRAND_CHARM_OF_VITA_41_45, item);
                    if (item.getPlusLifeStat() == 45)
                        addPerfectItem(Category.ANY_SKILLTAB_GRAND_CHARM_OF_VITA_41_45, item);
                }
                if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) >= 7) { // Sharp prefix
                    addItem(Category.SHARP_GRAND_CHARM_OF_VITA_41_45, item);
                    if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 10 &&
                            item.getStat(D2ItemStats.ATTACK_RATING.statId) == 76 &&
                            item.getPlusLifeStat() == 45) {
                        addPerfectItem(Category.SHARP_GRAND_CHARM_OF_VITA_41_45, item);
                    }
                }
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 13 && item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) >= 13) {
                    addItem(Category.SHIMMERING_GRAND_CHARM_OF_VITA_41_45, item);
                    if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 15 && item.getPlusLifeStat() == 45) {
                        addPerfectItem(Category.SHIMMERING_GRAND_CHARM_OF_VITA_41_45, item);
                    }
                }
            }
        }
    }



    private void handleLargeCharm(D2Item item) {
        // Sharp Large Charm of Vita (6 max damage, 48 attack rating, 35 life)
        if (item.getPlusLifeStat() >= 31) {
            if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) >= 4) {
                addItem(Category.SHARP_LARGE_CHARM_OF_VITA_31_35, item);
                if (item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 6 &&
                        item.getStat(D2ItemStats.ATTACK_RATING.statId) == 48 &&
                        item.getPlusLifeStat() == 35) {
                    addPerfectItem(Category.SHARP_LARGE_CHARM_OF_VITA_31_35, item);
                }
            }
        }
    }

    private void handleSmallCharm(D2Item item) {
        // Shimmering Small Charm of Good Luck (4-5 resist, 6-7 mf)
        // Shimmering Small Charm of Vita
        // Fine Small Charm of Vita (3/20/20)
        if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 3 && item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) >= 3) {
            if (item.getPlusLifeStat() >= 16) {
                addItem(Category.SHIMMERING_SMALL_CHARM_OF_VITA, item);
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 5 && item.getPlusLifeStat() == 20) {
                    addPerfectItem(Category.SHIMMERING_SMALL_CHARM_OF_VITA, item);
                }
            } else if (item.getStat(D2ItemStats.MAGIC_FIND.statId) >= 6) {
                addItem(Category.SHIMMERING_SMALL_CHARM_OF_GOOD_LUCK, item);
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 5 && item.getStat(D2ItemStats.MAGIC_FIND.statId) == 7) {
                    addPerfectItem(Category.SHIMMERING_SMALL_CHARM_OF_GOOD_LUCK, item);
                }
            }
        } else if (item.getPlusLifeStat() >= 16) {
            if (item.hasStat(D2ItemStats.ATTACK_RATING.statId) && item.hasStat(D2ItemStats.MAX_DAMAGE.statId)) {
                addItem(Category.FINE_SMALL_CHARM_OF_VITA, item);
                if (item.getPlusLifeStat() == 20 && item.getStat(D2ItemStats.ATTACK_RATING.statId) == 20 &&
                        item.getStat(D2ItemStats.MAX_DAMAGE.statId) == 3) {
                    addPerfectItem(Category.FINE_SMALL_CHARM_OF_VITA, item);
                }
            }
        }
    }



    private void handleAmulet(D2Item item) {
        //  Fortuitous Amulet of Luck (up to 50 mf)
        if (item.getStat(D2ItemStats.MAGIC_FIND.statId) >= 37) {
            if (item.getName().toLowerCase().contains("fortuitous") && item.getName().toLowerCase().contains("of luck")) {
                addItem(Category.FORTUITOUS_AMULET_OF_LUCK, item);
                if (item.getStat(D2ItemStats.MAGIC_FIND.statId) == 50) {
                    addPerfectItem(Category.FORTUITOUS_AMULET_OF_LUCK, item);
                }
            }
        }
    }

    private void handleRing(D2Item item) {
        // Fortuitous Ring of Fortune (up to 40 mf)
        if (item.getStat(D2ItemStats.MAGIC_FIND.statId) >= 27) {
            if (item.getName().toLowerCase().contains("fortuitous") && item.getName().toLowerCase().contains("of fortune")) {
                addItem(Category.FORTUITOUS_RING_OF_FORTUNE, item);
                if (item.getStat(D2ItemStats.MAGIC_FIND.statId) == 40) {
                    addPerfectItem(Category.FORTUITOUS_RING_OF_FORTUNE, item);
                }
            }
        }
    }

    // Ruby Jewel of Fervor (30-40 %ed, 15 ias)
    // Scintillating Jewel of Fervor (10-15 resists, 15 ias)
    private void handleJewel(D2Item item) {
        if (item.getStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId) == 15) {
            if (item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) >= 31) {
                addItem(Category.JEWEL_15_40, item);
                if (item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) == 40) {
                    addPerfectItem(Category.JEWEL_15_40, item);
                }
            } else if (item.getStat(D2ItemStats.FIRE_RESIST.statId) >= 11 && item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) >= 11) {
                addItem(Category.SCINTILLATING_JEWEL_OF_FERVOR, item);
                if (item.getStat(D2ItemStats.FIRE_RESIST.statId) == 15) {
                    addPerfectItem(Category.SCINTILLATING_JEWEL_OF_FERVOR, item);
                }
            }
        }

    }




    // 3 socket + Speed
    // 3 socket + 20 FCR
    // 3 socket + 20-30 dex
    // 3 socket + 20-30 str
    // 3 socket + damage reduction
    // 3 socket + 80-100 life
    private void handleCirclet(D2Item item) {
        if (item.getSockets() == 3) {
            if (item.getStat(D2ItemStats.FASTER_RUN_WALK_SPEED.statId) == 30) {
                addPerfectItem(Category.THREE_SOCKET_CIRCLET_30_FRW, item);
            } else if (item.getStat(D2ItemStats.FASTER_CAST_RATE.statId) == 20) {
                addPerfectItem(Category.THREE_SOCKET_CIRCLET_20_FCR, item);
            } else if (item.getStat(D2ItemStats.STRENGTH.statId) >= 21) {
                addItem(Category.THREE_SOCKET_CIRCLET_21_PLUS_STR, item);
                if (item.getStat(D2ItemStats.STRENGTH.statId) == 30) {
                    addPerfectItem(Category.THREE_SOCKET_CIRCLET_21_PLUS_STR, item);
                }
            } else if (item.getStat(D2ItemStats.DEXTERITY.statId) >= 21) {
                addItem(Category.THREE_SOCKET_CIRCLET_21_PLUS_DEX, item);
                if (item.getStat(D2ItemStats.DEXTERITY.statId) == 30) {
                    addPerfectItem(Category.THREE_SOCKET_CIRCLET_21_PLUS_DEX, item);
                }
            } else if (item.getStat(D2ItemStats.DAMAGE_REDUCTION.statId) >= 10) {
                if (item.getName().toLowerCase().contains("of life everlasting")) {
                    addItem(Category.THREE_SOCKET_CIRCLET_OF_LIFE_EVERLASTING, item);
                    if (item.getStat(D2ItemStats.DAMAGE_REDUCTION.statId) == 25) {
                        addPerfectItem(Category.THREE_SOCKET_CIRCLET_OF_LIFE_EVERLASTING, item);
                    }
                }
            } else if (item.getPlusLifeStat() >= 81) {
                addItem(Category.THREE_SOCKET_CIRCLET_OF_THE_WHALE, item);
                if (item.getPlusLifeStat() == 100) {
                    addPerfectItem(Category.THREE_SOCKET_CIRCLET_OF_THE_WHALE, item);
                }
            }
        }
    }

    private void handleGloves(D2Item item) {
        // +3 jav skills + 20 ias
        // +3 bow skills + 20 ias
        // +3 martial arts + 20 ias
        if (item.getStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId) == 20) {
            if (item.getSkillTabBonusLevel(SkillTab.AMAZON_JAVELIN_AND_SPEAR_SKILLS) == 3) {
                addPerfectItem(Category.THREE_JAV_SKILLS_20_IAS_GLOVES, item);
            }
            if (item.getSkillTabBonusLevel(SkillTab.AMAZON_BOW_AND_CROSSBOW_SKILLS) == 3) {
                addPerfectItem(Category.THREE_BOW_SKILLS_20_IAS_GLOVES, item);
            }
            if (item.getSkillTabBonusLevel(SkillTab.ASSASSIN_MARTIAL_ARTS) == 3) {
                addPerfectItem(Category.THREE_MARTIAL_ARTS_SKILLS_20_IAS_GLOVES, item);
            }
        }
    }



    private void handleAmazonJavelin(D2Item item) {
        if (item.getStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId) >= 20) {
            int ias = item.getStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId);
            int jav = item.getSkillTabBonusLevel(SkillTab.AMAZON_JAVELIN_AND_SPEAR_SKILLS);
            int amazon = item.getClassSkillBonusLevel(CharacterClass.AMAZON);
            if (jav + amazon >= 4) {
                if (jav == 6) {
                    if (ias == 40) addPerfectItem(Category.SIX_JAV_SKILLS_40_IAS, item);
                    if (ias == 30) addPerfectItem(Category.SIX_JAV_SKILLS_30_IAS, item);
                    if (ias == 20) addPerfectItem(Category.SIX_JAV_SKILLS_20_IAS, item);
                } else if (jav == 5) {
                    if (ias == 40) addPerfectItem(Category.FIVE_JAV_SKILLS_40_IAS, item);
                    if (ias == 30) addPerfectItem(Category.FIVE_JAV_SKILLS_30_IAS, item);
                    if (ias == 20) addPerfectItem(Category.FIVE_JAV_SKILLS_20_IAS, item);
                } else if (jav == 4) {
                    if (ias == 40) addPerfectItem(Category.FOUR_JAV_SKILLS_40_IAS, item);
                    if (ias == 30) addPerfectItem(Category.FOUR_JAV_SKILLS_30_IAS, item);
                    if (ias == 20) addPerfectItem(Category.FOUR_JAV_SKILLS_20_IAS, item);
                } else if (jav == 3) {
                    if (amazon == 2) {
                        if (ias == 40) addPerfectItem(Category.TWO_ZON_3_JAV_SKILLS_40_IAS, item);
                        if (ias == 30) addPerfectItem(Category.TWO_ZON_3_JAV_SKILLS_30_IAS, item);
                        if (ias == 20) addPerfectItem(Category.TWO_ZON_3_JAV_SKILLS_20_IAS, item);
                    } else if (amazon == 1) {
                        if (ias == 40) addPerfectItem(Category.ONE_ZON_3_JAV_SKILLS_40_IAS, item);
                        if (ias == 30) addPerfectItem(Category.ONE_ZON_3_JAV_SKILLS_30_IAS, item);
                        if (ias == 20) addPerfectItem(Category.ONE_ZON_3_JAV_SKILLS_20_IAS, item);
                    }
                } else if (jav == 2) {
                    if (amazon == 2) {
                        if (ias == 40) addPerfectItem(Category.TWO_ZON_2_JAV_SKILLS_40_IAS, item);
                        if (ias == 30) addPerfectItem(Category.TWO_ZON_2_JAV_SKILLS_30_IAS, item);
                        if (ias == 20) addPerfectItem(Category.TWO_ZON_2_JAV_SKILLS_20_IAS, item);
                    }
                }
            }
        }
    }




    enum Category {
        FOUR_SOCKET_ARCHON_PLATE_OF_THE_WHALE(0),
        FOUR_SOCKET_ANY_ARMOR_OF_THE_WHALE(1),
        FOUR_SOCKET_ANY_ARMOR_OF_STABILIITY(2),
        JEWELERS_MONARCH_OF_DEFLECTING(3),
        JEWELERS_MONARCH_OF_SIMPLICITY(4),
        FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_RESIST(5),
        FOUR_SOCKET_SACRED_TARGE_OF_DEFLECTING_HIGH_DAMAGE(6),
        FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_RESIST(7),
        FOUR_SOCKET_PALADIN_SHIELD_OF_DEFLECTING_HIGH_DAMAGE(8),
        PCOMBAT_GRAND_CHARM_OF_VITA_36_40(9),
        SHARP_GRAND_CHARM_OF_VITA_36_40(10),
        SHIMMERING_GRAND_CHARM_OF_VITA_36_40(11),
        PCOMBAT_GRAND_CHARM_OF_VITA_41_45(12),
        SHARP_GRAND_CHARM_OF_VITA_41_45(13),
        SHIMMERING_GRAND_CHARM_OF_VITA_41_45(14),
        ANY_SKILLTAB_GRAND_CHARM_OF_VITA_36_40(15),
        ANY_SKILLTAB_GRAND_CHARM_OF_VITA_41_45(16),
        SHARP_LARGE_CHARM_OF_VITA_31_35(17),
        SHIMMERING_SMALL_CHARM_OF_GOOD_LUCK(18),
        SHIMMERING_SMALL_CHARM_OF_VITA(19),
        FINE_SMALL_CHARM_OF_VITA(20),
        FORTUITOUS_AMULET_OF_LUCK(21),
        FORTUITOUS_RING_OF_FORTUNE(22),
        JEWEL_15_40(23),
        SCINTILLATING_JEWEL_OF_FERVOR(24),
        THREE_SOCKET_CIRCLET_30_FRW(25),
        THREE_SOCKET_CIRCLET_20_FCR(26),
        THREE_SOCKET_CIRCLET_21_PLUS_DEX(27),
        THREE_SOCKET_CIRCLET_21_PLUS_STR(28),
        THREE_SOCKET_CIRCLET_OF_LIFE_EVERLASTING(29),
        THREE_SOCKET_CIRCLET_OF_THE_WHALE(30),
        THREE_JAV_SKILLS_20_IAS_GLOVES(31),
        THREE_BOW_SKILLS_20_IAS_GLOVES(32),
        THREE_MARTIAL_ARTS_SKILLS_20_IAS_GLOVES(33),
        SIX_JAV_SKILLS_40_IAS(34),
        SIX_JAV_SKILLS_30_IAS(35),
        SIX_JAV_SKILLS_20_IAS(36),
        FIVE_JAV_SKILLS_40_IAS(37),
        FIVE_JAV_SKILLS_30_IAS(38),
        FIVE_JAV_SKILLS_20_IAS(39),
        FOUR_JAV_SKILLS_40_IAS(40),
        FOUR_JAV_SKILLS_30_IAS(41),
        FOUR_JAV_SKILLS_20_IAS(42),
        TWO_ZON_3_JAV_SKILLS_40_IAS(43),
        TWO_ZON_3_JAV_SKILLS_30_IAS(44),
        TWO_ZON_3_JAV_SKILLS_20_IAS(45),
        TWO_ZON_2_JAV_SKILLS_40_IAS(46),
        TWO_ZON_2_JAV_SKILLS_30_IAS(47),
        TWO_ZON_2_JAV_SKILLS_20_IAS(48),
        ONE_ZON_3_JAV_SKILLS_40_IAS(49),
        ONE_ZON_3_JAV_SKILLS_30_IAS(50),
        ONE_ZON_3_JAV_SKILLS_20_IAS(51),
        ;

        int index;

        Category(int index) { this.index = index; }
    }
}
