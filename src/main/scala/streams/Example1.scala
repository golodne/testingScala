import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import streams.Example2.outputFile

//import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.ActorMaterializer

import akka.stream.scaladsl.Source

object Example1 extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val source: Source[Int,NotUsed] = Source(1 to 100)

  val outfib = Paths.get("d:\\test\\fib.txt")
  val sink: Sink[ByteString,Future[IOResult]] = FileIO.toPath(outfib, Set(CREATE,WRITE,APPEND))

  //source.runForeach(i => println(i))
  ///val done: Future[Done] = source.runForeach(i => println(i))
 // done.onComplete(_ => system.terminate())
//1 1 2 3 5 8 13
  //val factorials = source.scan(BigInt(1))((acc,next) => acc * next)
  //записать числа Фибоначи в выходной файл
  //factorials.map(num => ByteString(s"$num\n")).runWith(sink)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] =
    factorials.map(num => ByteString(s"$num\n")).runWith(FileIO.toPath(Paths.get("d:\\test\\fib.txt")))




}