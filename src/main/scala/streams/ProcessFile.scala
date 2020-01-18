package streams

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

import spray.json._

//import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.ActorMaterializer

case class EventStream(
                host: String,
                service: String,
                state: State,
                time: ZonedDateTime,
                description: String,
                tag: Option[String] = None,
                metric: Option[Double] = None
                )

object ProcessFile extends App with EventMarshalling {

  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")

  val inputFile = Paths.get("d:\\test\\in.txt")
  val outputFile = Paths.get("d:\\test\\out.txt")
  //my-host-1 | web-app | ok    | 2015-08-12T12:12:00.127Z | 5 tickets sold ||

  val source: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine*2)
    .map(_.decodeString("UTF8"))

  val parse: Flow[String, Event, NotUsed] =
    Flow[String].map(LogStreamProcessor.parseLineEx).collect { case Some(e) => e }

  val serialize: Flow[Event, ByteString, NotUsed] =
    Flow[Event].map(event => ByteString(event.toJson.compactPrint))

  val filterState = Error

  val filter: Flow[Event, Event, NotUsed] =
    Flow[Event].filter(_.state == filterState)

  /* in.txt
my-host-1 | web-app | ok    | 2015-08-12T12:12:00.127Z | 5 tickets sold ||
my-host-2 | web-app | error | 2015-08-12T12:12:03.127Z | exception	||
my-host-3 | web-app | wr
*/




  val composedFlow: Flow[ByteString,ByteString,NotUsed] =
                frame
/*
    my-host-1 | web-app | ok    | 2015-08-12T12:12:00.127Z | 5 tickets sold ||
    my-host-2 | web-app | error | 2015-08-12T12:12:03.127Z | exception	||
*/
                .via(parse)
/* Objects
    Event(my-host-1,web-app,Ok,2015-08-12T12:12:00.127Z,5 tickets sold,None,None)
    Event(my-host-2,web-app,Error,2015-08-12T12:12:03.127Z,exception,None,None)
*/
                .via(filter)
//Event(my-host-2,web-app,Error,2015-08-12T12:12:03.127Z,exception,None,None)
                .via(serialize)



  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(composedFlow).toMat(sink)(Keep.right)

runnableGraph.run().foreach { result =>
    println(s"finish ${result.status}")
    system.terminate()
  }

/*
  stream.runWith(Sink.foreach{ result =>
    println(result.toString)
    system.terminate()
  })
*/






}

