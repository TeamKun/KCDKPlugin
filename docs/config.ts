

interface Config {
    timeLimit: Time | null
    teams: Team[]
    gamemode: String
    showBossBar: boolean
    EndCondition: EndCondition[]
}

interface Time {
    hour: number
    minutes: number
    second: number
}

interface Team {
    name: String
    displayName: String
    armorColor: number
    readyLocation: Location | null
    respawnLocation: Location
    stock: number
    waitingTime: Time
    effects: Effect[]
    roles: Role[]
}

interface Role {
    name: String
    displayName: String | null
    armorColor: number | null
    readyLocation: Location | null
    respawnLocation: Location
    stock: number | null
    waitingTime: Time | null
    effects: Effect[]
    extendsEffects: boolean
    extendsItem: boolean
}

interface Location {
    world: String
    x: number
    y: number
    z: number
    yaw: number
    pitch: number
}

interface EndCondition {
    message:String
}

interface CompositEndCondition extends EndCondition {
    condition: EndCondition[]
}

interface Effect {
    name:String
    second: number
    amplifer: number
    hideParticles: boolean
}

interface Beacon extends EndCondition {
    location: Location
    hitpoint: number
}

interface Extermination extends EndCondition {
    team: String
}

interface Ticket extends EndCondition {
    team: String
    count: number
}