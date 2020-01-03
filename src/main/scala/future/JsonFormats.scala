package future
import future.FutureServer.{Event, Route, TicketInfo, Weather}
import spray.json.DefaultJsonProtocol
object JsonFormats {
  import DefaultJsonProtocol._
  implicit val eventJsonFormat = jsonFormat2(Event)
  implicit val routeJsonFormat = jsonFormat1(Route)
  implicit val WeatherJsonFormat = jsonFormat2(Weather)
  implicit val tiketJsonFormat = jsonFormat4(TicketInfo)

}
