<#assign prop = server.getConfigProperties()>
settings:
  minimum-api: none
allow-end: ${prop["settings.allow-end"]!"false"}
  warn-on-overload: true
  permissions-file: permissions.yml
  update-folder: update
  plugin-profiling: false
  connection-throttle: 4000
  query-plugins: true
  deprecated-verbose: default
  shutdown-message: Server closed
spawn-limits:
  axolotls: 5
  water-underground-creature: 5
  water-ambient: 20
  monsters: 70
  animals: 15
  water-animals: 5
  ambient: 15
chunk-gc:
  period-in-ticks: 600
  load-threshold: 0
ticks-per:
  axolotl-spawns: 1
  water-underground-creature-spawns: 1
  water-ambient-spawns: 1
  water-spawns: 1
  ambient-spawns: 1
  animal-spawns: 400
  monster-spawns: 1
  autosave: 6000
aliases: now-in-commands.yml
database:
  username: root
  isolation: SERIALIZABLE
  driver: localhost
  password: password
  url: jdbc:sqlite:{DIR}{NAME}.db
