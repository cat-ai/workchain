package io.cat.ai.workchain

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.util.UUID

import io.cat.ai.workchain.app.api.TaskWrapper._
import io.cat.ai.workchain.app.{WorkChain, WorkChainSession}
import io.cat.ai.workchain.core.executor.{ManageableParallelizableDagExecutorGroup, ParallelMultiThreaded}
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SimpleTest extends AnyFlatSpec {

  lazy val workChainApp: WorkChain = WorkChainEnv.application

  def randString: String = s"${UUID.randomUUID}"

  val bohemianRhapsodyText: Seq[String] = Seq[String](
    "Is this the real life? Is this just fantasy?",
    "Caught in a landslide, no escape from reality",
    "Open your eyes, look up to the skies and see",
    "I'm just a poor boy, I need no sympathy",
    "Because I'm easy come, easy go, little high, little low",
    "Any way the wind blows doesn't really matter to me, to me"
  )

  val textBuffer: mutable.ArrayBuffer[String] = new ArrayBuffer[String]

  val readBuffers: mutable.ArrayBuffer[BufferedReader] = new ArrayBuffer[BufferedReader]
  val writeBuffers: mutable.ArrayBuffer[BufferedWriter] = new ArrayBuffer[BufferedWriter]
  val emptyContentFileBuff: mutable.ArrayBuffer[String] = new ArrayBuffer[String]


  trait NamedTask extends Runnable {
    def name: String
  }

  def ioNamedTask(taskName: String): NamedTask = new NamedTask {

    val source: String = getClass.getResource(s"/$taskName.txt").getPath

    val rBuf: BufferedReader = new BufferedReader(new FileReader(source), 1024)

    readBuffers += rBuf

    override def name: String = taskName

    override def run(): Unit = {
      Thread.sleep(1000)

      Option(rBuf.readLine) match {
        case Some(text) =>
          println(text)
          textBuffer += text

        case None =>
          val wBuf: BufferedWriter = new BufferedWriter(new FileWriter(s"$source", true))
          emptyContentFileBuff += source
          writeBuffers += wBuf
          wBuf.write(randString)
          wBuf.flush()
      }
    }
  }

  "Chained tasks [file2Task, file3Task, file4Task, file6Task, file8Task, file9Task] " should " be ordered" in {

    workChainApp.applyExecutionMode(ParallelMultiThreaded(nThreads = 4, parallelism = 2))

    val session: WorkChainSession = workChainApp.createNewSession()

    val file0Task = nret(ioNamedTask("file0"))
    val file1Task = nret(ioNamedTask("file1"))
    val file1_1Task = ret(ioNamedTask("file1_1"))

    val file2Task = nret(ioNamedTask("file2"))
    val file3Task = nret(ioNamedTask("file3"))

    val file3_5Task = ret(ioNamedTask("file3_5"))

    val file4Task = nret(ioNamedTask("file4"))
    val file3_6Task = ret(ioNamedTask("file3_6"))
    val file5Task = nret(ioNamedTask("file5"))
    val file6Task = ret(ioNamedTask("file6"))
    val file7Task = ret(ioNamedTask("file7"))
    val file8Task = ret(ioNamedTask("file8"))
    val file9Task = ret(ioNamedTask("file9"))

    session.addTasks(file0Task, file1Task, file1_1Task, file2Task, file3Task, file3_5Task, file3_6Task, file4Task, file5Task, file6Task, file7Task, file8Task, file9Task)

    import session.Implicits._

    file0Task   *-->  file2Task
    file1_1Task *-->  file1Task
    file1Task   *-->  file2Task
    file2Task   *-->  file3Task
    file3Task   *-->  file4Task
    file3_5Task *-->  file4Task
    file3_6Task *-->  file4Task
    file4Task   *-->  file6Task
    file6Task   *-->  file8Task
    file8Task   *-->  file9Task

    session.compute()

    Thread.sleep(25000)

    session.destroy()

    for (readBuff <- readBuffers) {
      readBuff.close()
    }

    val workChain = workChainApp.executorGroup.asInstanceOf[ManageableParallelizableDagExecutorGroup]

    assert(!workChain.isActive)
    assert(!workChain.hasJobs)
    assert(textBuffer == bohemianRhapsodyText)

    for (writeBuff <- writeBuffers) {
      writeBuff.close()
    }

    for (emptyContentFile <- emptyContentFileBuff)
      new FileWriter(emptyContentFile,false).close()
  }
}

