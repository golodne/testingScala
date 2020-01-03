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


  case class Weather(temperature: Int, precipitaition: Boolean)

  case class TicketInfo(ticketNr: String,
                        event: Option[Event] = None,
                        weather: Option[Weather] = None,
                        route: Option[Route] = None)


  case class EventRequest(name: String)
  case class EventResponse(event: Event)
  case class RouteResponse(route: Route)


  case class Event(location: String, time: Long)
  case class Route(address: String)

  def callEventService(request: EventRequest): EventResponse = {
    Thread.sleep(3000)
    EventResponse(Event("Vladimir from event",7777))
  }

  def callRouteService(event: Option[Event])(implicit ex: ExecutionContext): RouteResponse =  {
    Thread.sleep(2000)
    RouteResponse(Route("route: Saratov, nagory proezd 4"))
  }

  def callWeatherXService(ticketInfo: TicketInfo)(implicit ex: ExecutionContext): Future[Option[Weather]] = Future {
    Thread.sleep(500)
    Some(Weather(43,false))
  }

  def callWeatherYService(ticketInfo: TicketInfo)(implicit ex: ExecutionContext): Future[Option[Weather]] = Future {
    Thread.sleep(1000)
    Some(Weather(50,true))
  }

  //паралельный вызов с возратом 1-го результата (2 способа)
  def getWeather(ticketInfo: TicketInfo)(implicit ex: ExecutionContext): Future[TicketInfo] = {

    val futureSrv1: Future[Option[Weather]] = callWeatherXService(ticketInfo).recover { case _ => None }
    val futureSrv2: Future[Option[Weather]] = callWeatherYService(ticketInfo).recover { case _ => None }

    val futures: List[Future[Option[Weather]]] = List(futureSrv1,futureSrv2)

    //1 вариант поиска future
    //val firstResponse: Future[Option[Weather]] = Future.firstCompletedOf(futures)

    //2 вариант
    val firstResponse: Future[Option[Weather]] = Future.find(futures)(x => !x.isEmpty).map(_.flatten)

    firstResponse.map {
      weatherResp =>
        ticketInfo.copy(weather = weatherResp)
    }
  }

  //объединение двух параллельных вызовов




  def getEvent(ticketInfo: TicketInfo)(implicit ex: ExecutionContext): Future[TicketInfo] = Future {
   // throw new IllegalArgumentException("tststs")
    val eventRequest: EventRequest = EventRequest(ticketInfo.ticketNr)
    val responseEvent = callEventService(eventRequest)
    ticketInfo.copy(event = Some(responseEvent.event))
  }

  def getRouteByEvent(ticketInfo: TicketInfo)(implicit ex: ExecutionContext): Future[TicketInfo] = Future {
    // throw new IllegalArgumentException("uuuups")
    val responseRoute = callRouteService(ticketInfo.event)
    ticketInfo.copy(route = Some(responseRoute.route))
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

              val ticketInfo: TicketInfo = TicketInfo("55555")

              val fweather: Future[TicketInfo] = getWeather(ticketInfo);
              val futureRoute:Future[TicketInfo] = getEvent(ticketInfo).flatMap {
                ticket =>
                  //println(s"finded ticket $ticket")
                  getRouteByEvent(ticket).recover {
                    case e: Exception => TicketInfo(ticket.ticketNr,ticketInfo.event)
                  }
              }.recover{
                case e: Exception => TicketInfo("error from getEvent 5555") //return
              }

              for {
                w <- fweather
                r <- futureRoute
              } yield ticketInfo.copy(weather = w.weather,route = r.route)
            }

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