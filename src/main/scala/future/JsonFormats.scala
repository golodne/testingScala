package future
import future.FutureServer.{Event, Location, PublicTransportAdvice, Route, RouteByCar, TicketInfo, TravelAdvice, Weather}
import spray.json.DefaultJsonProtocol
object JsonFormats {
  import DefaultJsonProtocol._
  implicit val eventJsonFormat = jsonFormat2(Event)
  implicit val routeJsonFormat = jsonFormat1(Route)
  implicit val WeatherJsonFormat = jsonFormat2(Weather)

  implicit val LocationJsonFormat = jsonFormat2(Location)
  implicit val RouteByCarJsonFormat = jsonFormat2(RouteByCar)
  implicit val PublicTransportAdviceJsonFormat = jsonFormat1(PublicTransportAdvice)

  implicit val TravelAdviceJsonFormat = jsonFormat2(TravelAdvice)
  implicit val tiketJsonFormat = jsonFormat5(TicketInfo)

}
