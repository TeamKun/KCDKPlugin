interface Config {
    gamemode: Gamemode
    bossbar: Bossbar | null
    timeLimit: Time | null
    startupCommands: string[]
    shutdownCommands: string[]
    teams: Team[]
    endConditions: EndCondition[]
}

type Gamemode = "ADVENTURE" | "SURVIVAL"

interface Bossbar {
    mcid: string
}

interface Time {
    hours: number
    minutes: number
    seconds: number
}

interface Team {
    name: string
    displayName: string
    armorColor: string
    respawnCount: number
    readyLocation: ReadyLocation | null
    respawnLocation: Location
    effects: Effect[]
    roles: Role[]
}

interface ReadyLocation extends Location {
    waitingTime: Time
}

interface Location {
    world: string
    x: number
    y: number
    z: number
    yaw: number
    pitch: number
}

interface Role {
    name: string
    displayName: string | null
    armorColor: string | null
    readyLocation: ReadyLocation | null
    respawnLocation: Location | null
    respawnCount: number | null
    effects: Effect[]
    extendsEffects: boolean
    extendsItem: boolean
}

interface Effect {
    name: string
    seconds: number
    amplifier: number
    hideParticles: boolean
}

interface EndCondition {
    type: EndConditionType
    message: string
}

type EndConditionType = "extermination" | "beacon" | "ticket" | "composite"

interface CompositeEndCondition extends EndCondition {
    type: "composite"
    conditions: EndCondition[]
    operator: Operator
}

type Operator = "OR" | "AND"

interface BeaconEndCondition extends EndCondition {
    type: "beacon"
    location: Location
    hitpoint: number
}

interface ExterminationEndCondition extends EndCondition {
    type: "extermination"
    team: string
}

interface TicketEndCondition extends EndCondition {
    type: "ticket"
    team: string
    count: number
}