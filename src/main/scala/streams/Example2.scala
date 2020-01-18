package streams

import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent._
import java.nio.file._
import java.nio.file.StandardOpenOption._

import akka.Done
import akka.actor.Status.Success
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, RunnableGraph, Sink, Source}
import akka.stream.ActorMaterializer


object Example2 extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val inputFile = Paths.get("d:\\test\\source.txt")
  val outputFile = Paths.get("d:\\test\\out.txt")

  val source: Source[ByteString,Future[IOResult]] = FileIO.fromPath(inputFile)
  val sink: Sink[ByteString,Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE,WRITE,APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

  //при получении результата - удаляем исходный файл
  runnableGraph.run().foreach (result => inputFile.toFile.delete())




}
