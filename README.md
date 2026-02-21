# 🏰 ClanMaster - Advanced Clan Plugin for Minecraft

[![Version](https://img.shields.io/badge/version-1.0.0-blue)]()
[![Spigot](https://img.shields.io/badge/Spigot-1.19+-orange)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()

**Author:** _4rubka_

---

## About

**ClanMaster** is a powerful and feature-rich clan management plugin for Minecraft servers. Create clans, recruit members, declare wars, earn achievements, and dominate the battlefield!

Perfect for PvP servers, factions, and any server that wants a deep clan system with modern features.

---

## Features

### For Players Without a Clan
- Beautiful GUI browser to explore all clans
- Top clans leaderboard by level and war points
- Player statistics (K/D ratio, kills, deaths)
- Command help guide

### For Clan Members
- Full clan management GUI
- Invite, kick, promote, and demote members
- Clan settings (friendly fire, home, prefix)
- Level-based bonuses system
- Clan chest locking
- Allies and enemies diplomacy

### War System
- Declare war on other clans
- Track war kills in real-time
- War points leaderboard
- Rewards for winning wars
- Peace treaties

### Achievements & Titles
- 5+ unique achievements (First Blood, Warrior, Veteran, Legend, War Winner)
- Custom clan titles
- XP rewards for achievements
- Daily kill quests

### Activity Tracking
- Last activity status
- Win/loss statistics
- War history
- Member contribution tracking

### Immersive Experience
- Sound effects on level up
- Particle effects for events
- Action bar notifications
- Broadcast messages

---

## Commands

### Basic Commands
```
/clan              - Open clan menu
/clan top          - View top clans by level
/clan wartop       - View top clans by war points
/clan list         - List all clans
/clan info [clan]  - Clan information
/clan stats        - Detailed clan statistics
/clan create <name> - Create a new clan
/clan invite <player> - Invite a player
/clan join         - Join a clan
/clan leave        - Leave your clan
```

### Leader Commands
```
/clan transfer <player>    - Transfer leadership
/clan prefix <text>        - Set clan prefix
/clan title <name>         - Set clan title
/clan motd <message>       - Set message of the day
/clan desc <description>   - Set clan description
/clan sethome              - Set clan home location
/clan delhome              - Delete clan home
/clan home                 - Teleport to clan home
/clan pvp                  - Toggle friendly fire
/clan war <clan>           - Declare war
/clan peace <clan>         - Make peace
/clan ally <add/remove>    - Manage allies
/clan enemy <add/remove>   - Manage enemies
/clan deposit <amount>     - Deposit points to bank
/clan withdraw <amount>    - Withdraw points from bank
```

### Admin Commands
```
/clanadmin save            - Save all data
/clanadmin reload          - Reload configuration
/clanadmin disband <clan>  - Disband a clan
/clanadmin info <clan>     - View clan info
/clanadmin givexp <clan> <amount>  - Give XP
/clanadmin setlevel <clan> <level> - Set clan level
/clanadmin list            - List all clans
```

---

## Configuration

### Main Config (config.yml)
```yaml
# Language: en, uk, de, fr
language: en

# Economy
costs:
  create: 500.0
  war: 1000.0
  peace: 500.0

# Limits
limits:
  max-members: 20
  max-ally-clans: 5
  max-active-wars: 3

# Progression
progress:
  xp-per-level: 1000
  xp-per-kill: 10
  xp-war-kill-bonus: 25

# Storage: JSON, SQLITE, MYSQL
storage:
  type: JSON

# Feature toggles - disable any feature you don't want
enabled-features:
  clan-home: true
  clan-wars: true
  achievements: true
  bonuses: false  # Disabled by default

# Menu text customization - all text in English by default
menu-text:
  main-title: "&#ff4faf&l✦ &fClan Menu"
  stats: "&#5fd9ffStatistics"
  bonuses: "&#6cffc9Bonuses"
  manage: "&#f3b4ffManagement"
  settings: "&#ffd166Settings"
  wars: "&c&lWars"
  achievements: "&#5fd9ffAchievements"
```

---

## Clan Ranks

| Rank | Permissions |
|------|-------------|
| **LEADER** | Full control |
| **OFFICER** | Manage members |
| **MEMBER** | Basic access |

---

## Achievements

| Achievement | Description | Reward |
|-------------|-------------|--------|
| First Blood | Get first kill | 100 XP |
| Warrior | 100 kills | 500 XP |
| Veteran | 500 kills | 2000 XP |
| Legend | 1000 kills | 5000 XP |
| War Winner | Win 10 wars | 3000 XP |

---

## Dependencies

### Required
- Spigot/Paper 1.19+

### Optional
- **Vault** - Economy support
- **PlaceholderAPI** - Placeholder support (for TAB, scoreboards, etc.)
- **Essentials/CMI** - Integration
- **TAB/TabList** - Tab list integration

---

## PlaceholderAPI Support

ClanMaster provides **40+ placeholders** for use in TAB, scoreboards, chat formats, and more!

### Basic Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_name%` | Clan name |
| `%clanmaster_level%` | Clan level |
| `%clanmaster_xp%` | Current XP |
| `%clanmaster_xp_needed%` | XP needed for next level |
| `%clanmaster_next_level%` | Next level number |
| `%clanmaster_xp_progress%` | Current XP progress |
| `%clanmaster_xp_needed_next%` | XP needed for next level |
| `%clanmaster_members%` | Total member count |
| `%clanmaster_online_members%` | Online members count |
| `%clanmaster_max_members%` | Max members limit |
| `%clanmaster_leader%` | Leader name |
| `%clanmaster_rank%` | Player rank |

### Economy Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_coins%` | Clan coins |
| `%clanmaster_points%` | Clan points |
| `%clanmaster_player_points%` | Player's contributed points |
| `%clanmaster_bonus_coins%` | Bonus coins for current level |
| `%clanmaster_bonus_xp%` | Bonus XP for current level |
| `%clanmaster_bonus_privilege%` | Bonus privilege |

### War Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_war_points%` | War points |
| `%clanmaster_wins%` | Wars won |
| `%clanmaster_losses%` | Wars lost |
| `%clanmaster_active_wars%` | Current active wars |
| `%clanmaster_war_kills%` | Total war kills |
| `%clanmaster_war_status%` | War status (war/peace/none) |

### Player Stats Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_kills%` | Player kills |
| `%clanmaster_deaths%` | Player deaths |
| `%clanmaster_kd%` | K/D ratio |
| `%clanmaster_daily_kills%` | Daily kills |
| `%clanmaster_joindays%` | Days in clan |
| `%clanmaster_activity%` | Activity status |

### Diplomacy Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_allies%` | Allies count |
| `%clanmaster_enemies%` | Enemies count |
| `%clanmaster_ally_list%` | Comma-separated ally list |
| `%clanmaster_enemy_list%` | Comma-separated enemy list |

### Customization Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_title%` | Clan title |
| `%clanmaster_motd%` | Clan MOTD |
| `%clanmaster_description%` | Clan description |
| `%clanmaster_prefix%` | Clan prefix |
| `%clanmaster_clan_tag%` | Formatted clan tag |
| `%clanmaster_achievements%` | Achievement count |

### Leaderboard Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_top_name%` | Top clan name |
| `%clanmaster_top_level%` | Top clan level |

### Other Placeholders
| Placeholder | Description |
|-------------|-------------|
| `%clanmaster_friendlyfire%` | Friendly fire status |
| `%clanmaster_home_set%` | Home set status |

---

## TAB Configuration Example

```yaml
# In TAB config.yml
prefix: "%clanmaster_clan_tag%"
```

---

## 🚀 Installation

1. Download the latest `.jar` file
2. Place it in your server's `plugins` folder
3. Restart the server
4. Configure `config.yml` and `lang/*.yml` files
5. Done!

---

## Language Support

ClanMaster supports multiple languages:
- 🇬🇧 **English** (en) - Default
- 🇺🇦 **Ukrainian** (uk)
- 🇩🇪 **German** (de)
- 🇫🇷 **French** (fr)

Change language in `config.yml`:
```yaml
language: en
```

---

## GUI Menus

### No Clan Menu
- Player statistics display
- Top clans browser
- All clans list
- Command help

### Clan Member Menu
- Clan information
- Statistics & bonuses
- Member management
- Settings (PvP, home, prefix)
- Active wars
- Achievements

All menu text is customizable in `config.yml` and defaults to English!

---

## Tips for Server Owners

1. **Configure bonuses** to motivate players (or disable with `bonuses: false`)
2. **Use MySQL storage** for large servers
3. **Adjust clan creation cost** for your economy
4. **Enable sound effects** for better immersion
5. **Set up daily quests** for activity
6. **Customize menu text** in config to match your server theme
7. **Disable unwanted features** in `enabled-features` section
8. **Use PlaceholderAPI** for TAB and scoreboard integration

---

## License

This plugin is distributed under the MIT License.

---

## Support

Having issues or suggestions? Open an issue on our GitHub repository or join our Discord server.

---

- [Discord](https://discord.gg/mUz2k7X6ju)
