package future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.Success

object FutureServer {

  case class EventRequest(name: String)
  case class EventResponse(event: Event)



  case class Event(location: String, time: Long)

  def callEventService(request: EventRequest): EventResponse = {
    Thread.sleep(5000)
    return EventResponse(Event("Vladimir",7777))
  }

  def testFututre1()(implicit ex: ExecutionContext): Future[String] =  {

    val request = EventRequest("Saratov")
    //вызов в потоке Y
    val futureEvent: Future[Event] = Future {
      val resp: EventResponse = callEventService(request)
      resp.event
    }

    futureEvent.map(x => x.location)

  }

  def main(args: Array[String]) {

    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import JsonFormats._

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      get {
        concat(
          pathSingleSlash {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
          },
          path("ping") {
            complete("PONG!")
          },
          path("test") {
            complete {

              val request = EventRequest("Saratov")

              val eventFuture: Future[Event] = Future {
                val resp: EventResponse = callEventService(request)
                println(s"asinc result ${resp.event.toString}")
                resp.event
              }

              eventFuture.map(x => x)
            }
            // complete(testFututre1)
          }
        )
      }

    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}