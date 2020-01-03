package future
import future.FutureServer.{Event, Route, TicketInfo}
import spray.json.DefaultJsonProtocol
object JsonFormats {
  import DefaultJsonProtocol._
  implicit val eventJsonFormat = jsonFormat2(Event)
  implicit val routeJsonFormat = jsonFormat1(Route)
  implicit val tiketJsonFormat = jsonFormat3(TicketInfo)

}
