import docker.*
import testing.*
import LocalProcesses.*

// Register Task and its Commands for testing with container.
val runTestDbContainer = TaskKey[Unit]("runTestDbContainer", "Run DB container for testing.")
val shutDownTestDbContainer = TaskKey[Unit]("shutDownTestDbContainer", "Shut down DB container for testing.")
val testingDocker = new Testing()

testingDocker.tasks
runTestDbContainer := Def.sequential(testingDocker.upTesting).value
shutDownTestDbContainer := Def.sequential(testingDocker.downTesting).value
addCommandAlias("testWithDb", testingDocker.Commands.runAll)
addCommandAlias("testWithDB", testingDocker.Commands.runAll)
addCommandAlias("testDbUp", testingDocker.Commands.upDbAndCreateMinData)
addCommandAlias("testDBUp", testingDocker.Commands.upDbAndCreateMinData)

// Register Task and its Commands for run local db with container.
val runLocalDbContainer = TaskKey[Unit]("runLocalDbContainer", "Run DB container for local development.")
val shutDownLocalDbContainer = TaskKey[Unit]("shutDownLocalDbContainer", "Shut down DB container for local development.")
val localDbDocker = new LocalDb()

localDbDocker.tasks
runLocalDbContainer := Def.sequential(localDbDocker.upLocalDb).value
shutDownLocalDbContainer := Def.sequential(localDbDocker.downLocalDb).value
addCommandAlias("localDbUp", localDbDocker.Commands.up)
addCommandAlias("localDBUp", localDbDocker.Commands.up)
addCommandAlias("localDbDown", localDbDocker.Commands.down)
addCommandAlias("localDBDown", localDbDocker.Commands.down)

// Register Task and its Commands for run local otel with container.
val runLocalOtelContainer = TaskKey[Unit]("runLocalOtelContainer", "Run Otel container for local development.")
val shutDownLocalOtelContainer = TaskKey[Unit]("shutDownLocalOtelContainer", "Shut down Otel container for local development.")
val localOtelDocker = new LocalOtel()

localOtelDocker.tasks
runLocalOtelContainer := Def.sequential(localOtelDocker.upLocalOtel).value
shutDownLocalOtelContainer := Def.sequential(localOtelDocker.downLocalOtel).value
addCommandAlias("localOtelUp", localOtelDocker.Commands.up)
addCommandAlias("localOtelDown", localOtelDocker.Commands.down)

// run db and otel
addCommandAlias("localUp", ";localDbUp;localOtelUp")
addCommandAlias("localDown", ";localDbDown;localOtelDown")


// Register Task and its Commands for kill server and run server locally.
val forceKillServer = TaskKey[Unit]("forceKillServer", "force kill http server")

LocalProcesses.tasks
forceKillServer := Def.sequential(LocalProcesses.kill).value
addCommandAlias("kills", LocalProcesses.Commands.kill)
addCommandAlias("runs", LocalProcesses.Commands.startLocalServer)
