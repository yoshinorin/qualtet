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
val localDocker = new Local()

localDocker.tasks
runLocalDbContainer := Def.sequential(localDocker.upLocal).value
shutDownLocalDbContainer := Def.sequential(localDocker.downLocal).value
addCommandAlias("localDbUp", localDocker.Commands.up)
addCommandAlias("localDBUp", localDocker.Commands.up)
addCommandAlias("localDbDown", localDocker.Commands.down)
addCommandAlias("localDBDown", localDocker.Commands.down)

// Register Task and its Commands for kill server and run server locally.
val forceKillServer = TaskKey[Unit]("forceKillServer", "force kill http server")

LocalProcesses.tasks
forceKillServer := Def.sequential(LocalProcesses.kill).value
addCommandAlias("kills", LocalProcesses.Commands.kill)
addCommandAlias("runs", LocalProcesses.Commands.startLocalServer)
